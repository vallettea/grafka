/*
    Copyright 2013 Snips

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package net.snips.grafka

import akka.actor.{ActorSystem, Props, Actor, ActorRef, FSM, Stash}
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

// messages
case object ClearMessage // safe stop of all edges
case class SPMessages(number: Long) // start shortest path calculation
case class CreateVertex(name: String) // add a vertex to the graph
case class CreateEdge(v1: String, v2: String, weight: Double) // add an weighted edge 
case class GetVertexDegree(name: String) // number of connected nodes
case class GetVertexDistanceTable(name: String) // ask a vertex to send his distance table

// graph states
sealed trait StateGraph
case object Relaxed extends StateGraph // this is when all vertices are Idle
case object Computing extends StateGraph // when messages are exchange between nodes

// graph data state
sealed trait DataGraph
case object UninitializedGraph extends DataGraph
case class GraphData(vertices: Map[String, ActorRef],
                     vertexStates: Map[String, StateVertex],
                     nbMessages: Long) extends DataGraph


class GraphActor extends Actor with Stash with FSM[StateGraph, DataGraph] {

    startWith(Relaxed, UninitializedGraph)
    implicit val timeout = Timeout(1 second)


    when(Relaxed) {

        case Event(CreateVertex(name), UninitializedGraph) => {
            val vertex = context.actorOf(Props(new VertexActor(name)), name = name)
            vertex ! StartSP
            var vertices: Map[String, ActorRef] = Map(name -> vertex)
            var vertexStates: Map[String, StateVertex] = Map(name -> Active)
            var nbMessages: Long = 0
            goto(Computing) using GraphData(vertices, vertexStates, nbMessages)
        }

        case Event(GetVertexDistanceTable(name), GraphData(vertices, vertexStates, nbMessages)) => {
            val future = vertices(name) ? Cost
            val cost = Await.result(future, timeout.duration).asInstanceOf[Map[ActorRef, Double]]
            val cleanedCost = cost.map(s => (s._1.toString, s._2)).toMap
            sender ! cost
            stay using GraphData(vertices, vertexStates, nbMessages)
        }

        case Event(ClearMessage, GraphData(vertices, vertexStates, nbMessages)) => {
            vertices.values.foreach(vertex => vertex ! ShutdownVertexMessage)
            var newVertices: Map[String, ActorRef] = Map()
            var newVertexStates: Map[String, StateVertex] = Map()
            stay using GraphData(newVertices, newVertexStates, nbMessages)
        } 

    }
	
    when(Computing) {

        case Event(VertexIdle(name), GraphData(vertices, vertexStates, nbMessages)) => {
            var newVertexStates = vertexStates.updated(name, Idle)
            // println(newVertexStates)
            if (newVertexStates.values.toSet.contains(Active)) {
                stay using GraphData(vertices, newVertexStates, nbMessages)}
            else {
                unstashAll()
                println("Number of message for relaxation: " + nbMessages)
                goto(Relaxed) using GraphData(vertices, newVertexStates, nbMessages)
            }
        }

        case Event(GetVertexDistanceTable(name), GraphData(vertices, vertexStates, nbMessages)) => {
            stash()
            stay using GraphData(vertices, vertexStates, nbMessages)
        } 

        case Event(ClearMessage, GraphData(vertices, vertexStates, nbMessages)) => {
            stash()
            stay using GraphData(vertices, vertexStates, nbMessages)
        } 

    }

    // whenever the state of the graph
    whenUnhandled {
        case Event(SPMessages(number), GraphData(vertices, vertexStates, nbMessages)) => {
            var newNbMessages = nbMessages + number
            stay using GraphData(vertices, vertexStates, newNbMessages)
        }
        case Event(CreateVertex(name), GraphData(vertices, vertexStates, nbMessages)) => {
            val vertex = context.actorOf(Props(new VertexActor(name)), name = name)
            vertex ! StartSP
            var newVertices = vertices.updated(name, vertex)
            var newVertexStates = vertexStates.updated(name, Active)
            stay using GraphData(newVertices, newVertexStates, nbMessages)
        }

        case Event(CreateEdge(v1, v2, weight), GraphData(vertices, vertexStates, nbMessages)) => {
            vertices(v1) ! AddNeighbor(vertices(v2), weight)
            vertices(v2) ! AddNeighbor(vertices(v1), weight)
            vertices(v1) ! Flood
            stay using GraphData(vertices, vertexStates, nbMessages)
        }
        case Event(GetVertexDegree(name), GraphData(vertices, vertexStates, nbMessages)) => {
            val future = vertices(name) ? Degree
            val degree = Await.result(future, timeout.duration).asInstanceOf[Int]
            sender ! degree
            stay using GraphData(vertices, vertexStates, nbMessages)
        }
    }

}