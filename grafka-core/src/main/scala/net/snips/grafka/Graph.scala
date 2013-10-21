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

import akka.actor.{ActorSystem, Props, Actor}
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import java.util.UUID

object Graph {

    val system = ActorSystem.create("grafka")
    val graphActor = system.actorOf(Props[GraphActor], name = UUID.randomUUID().toString)
    implicit val timeout = Timeout(500 second)

    def addVertex(name: String) = graphActor ! CreateVertex(name)

    def addVertices(names: List[String]) = names.map(name => graphActor ! CreateVertex(name))

    def addEdge(v1: String, v2: String, weight: Double) = graphActor ! CreateEdge(v1, v2, weight)

    def getDegree(name: String): Int = {
        val future = graphActor ? GetVertexDegree(name)
        Await.result(future, timeout.duration).asInstanceOf[Int]
    }

    def getDistanceTable(name: String): Map[String, Double] = {
        val future = graphActor ? GetVertexDistanceTable(name)
        // geting the distance table once the graph is relaxed is fast 
        // but the query is postpone until this state which can be long
        Await.result(future, timeout.duration).asInstanceOf[Map[String, Double]]
    }

    // safe shutdown of all vertices
    def clear() = graphActor ! ClearMessage

}

