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

import org.scalatest.FunSuite
import org.specs._

class GrafkaTestSuite extends FunSuite {

    test("A graph object should be able to create vertices and edges") {
        val graph = Graph
        graph.addVertex("1")
        graph.addVertex("2")
        graph.addVertex("3")
        
        graph.addEdge("1", "2", 9.9)
        graph.addEdge("1", "3", 3.3)

        val degree = graph.getDegree("1")
    
        assert(degree == 2)

    }

    test("Test shortest path algo on small undirected graph") {
        val graph = Graph
        graph.addVertices(List("A","B","C","D","E","F","G","H"))
        graph.addEdge("G", "F", 4.0)
        graph.addEdge("F", "E", 2.0)
        graph.addEdge("F", "B", 3.0)
        graph.addEdge("G", "B", 3.0)
        graph.addEdge("A", "E", 10.0)
        graph.addEdge("E", "B", 4.0)
        graph.addEdge("A", "B", 4.0)
        graph.addEdge("B", "C", 2.0)
        graph.addEdge("E", "C", 1.0)
        graph.addEdge("E", "D", 2.0)
        graph.addEdge("D", "C", 2.0)
        graph.addEdge("H", "C", 3.0)
        

        val ft = graph.getDistanceTable("E")
        val solution = Map("F" -> 2.0, "B" -> 3.0, "H" -> 4.0, "D" -> 2.0, "E" -> 0.0, "C" -> 1.0, "G" -> 6.0, "A" -> 7.0)
        assert(ft.values.toSet == solution.values.toSet)

    }

}