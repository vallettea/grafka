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

import akka.actor.{Actor, ActorRef, FSM, Stash}
import scala.concurrent.duration._


case object Degree
case object Cost
case object StartSP
case object ShutdownVertexMessage
case object Flood
case class AddNeighbor(vertex: ActorRef, weight: Double)
case class DistanceVector(dv: Map[ActorRef, Double])
case class VertexIdle(name: String)

sealed trait StateVertex
case object Idle extends StateVertex
case object Active extends StateVertex

sealed trait DataVertex
case object UninitializedVertex extends DataVertex
case class VertexData(prop: String, 
                      neighbors: Map[ActorRef, Double], 
                      cost: Map[ActorRef, Double],
                      next: Map[ActorRef, ActorRef]) extends DataVertex

class VertexActor(vertexName: String) extends Actor with Stash with FSM[StateVertex, DataVertex] {

    startWith(Idle, UninitializedVertex)


    when(Idle) {
        // at creation the vertex is idle and unitialized
        case Event(StartSP, UninitializedVertex) => {
            var prop = vertexName
            var neighbors: Map[ActorRef, Double] = Map()
            var cost: Map[ActorRef, Double] = Map(self -> 0)
            var next: Map[ActorRef, ActorRef] = Map(self -> self)
            goto(Active) using VertexData(prop, neighbors, cost, next)
        }
        // it idle and dv sent, the vertex should go active with previous parameters
        case Event(DistanceVector(dv), VertexData(prop, neighbors, cost, next)) => {
            goto(Active) using VertexData(prop, neighbors, cost, next)
        }
        case Event(ShutdownVertexMessage, VertexData(prop, neighbors, cost, next)) => {
            context.stop(self)
            stay using VertexData(prop, neighbors, cost, next)
        }
    }

    when(Active, stateTimeout = 1 second) {
        // receive an distance vector, processes it and stays active
        case Event(DistanceVector(dv), VertexData(prop, neighbors, cost, next)) => {
            var changed = false
            var newCost = collection.mutable.Map[ActorRef, Double]() ++= cost
            var newNext = collection.mutable.Map[ActorRef, ActorRef]() ++= next
            val incrementedDV = dv.map(s => (s._1, s._2 + neighbors(sender))).toMap
            incrementedDV.foreach{ keyValue =>
                val (key, value) = (keyValue._1, keyValue._2)
                if (newCost.keySet.contains(key)) {
                    if (newCost(key) > value) {
                        newCost = newCost.updated(key, value)
                        newNext = newNext.updated(key, sender)
                        changed = true
                    }
                }
                else { 
                    newCost += (key -> value)
                    newNext += (key -> sender)
                    changed = true
                }
            }
            if (changed) self ! Flood
            val immutableNewCost = collection.immutable.Map() ++ newCost
            val immutableNewNext = collection.immutable.Map() ++ newNext
            stay using VertexData(prop, neighbors, immutableNewCost, immutableNewNext)
        }
        // if no distanceVector was received in the last second the vertex goes idle
        case Event(StateTimeout, VertexData(prop, neighbors, cost, next)) => {
            context.parent ! VertexIdle(prop)
            unstashAll()
            goto(Idle) using VertexData(prop, neighbors, cost, next)
        }

        // turn off the node
        case Event(ShutdownVertexMessage, VertexData(prop, neighbors, cost, next)) => {
            stash()
            stay using VertexData(prop, neighbors, cost, next)
        }
    }

    whenUnhandled {

        // to tell a vertex that an edge has been added
        case Event(AddNeighbor(vertex, weight), VertexData(prop, neighbors, cost, next)) => 
            stay using VertexData(prop, neighbors.updated(vertex, weight), cost, next)

        // flood means send all neighbors your new distance vector
        case Event(Flood, VertexData(prop, neighbors, cost, next)) => {
            var nbMessage = 0
            neighbors.keys.foreach{ vertex => 
                vertex ! DistanceVector(cost)
                nbMessage += 1
            }
            context.parent ! SPMessages(nbMessage)
            stay using VertexData(prop, neighbors, cost, next)
        }


        case Event(Degree, VertexData(prop, neighbors, cost, next)) => {
            sender ! neighbors.keys.toList.length
            stay using VertexData(prop, neighbors, cost, next)
        }

        // when asked for a distance vector
        case Event(Cost, VertexData(prop, neighbors, cost, next)) => {
            sender ! cost
            stay using VertexData(prop, neighbors, cost, next)
        }
    }


}