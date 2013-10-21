package net.snips.grafka.example

import net.snips.grafka.Graph

import scala.util.Random
import java.util.UUID

object Main extends App {

	// create a random graph with N vertices and S edges
    val N = 100
    val S = 3*N
    
    var start = System.currentTimeMillis
    val graph = Graph

    val id = UUID.randomUUID().toString
    graph.addVertex(id)
    var vertices = scala.collection.mutable.Set(id)

    for (i <- 0 until N) {
        // a new vertex
        val id = UUID.randomUUID().toString
        graph.addVertex(id)
        // select a random vertex from existing ones
        val randVertex = Random.shuffle(vertices.toList).head

        // connect them
        val weight: Double = java.lang.Math.random() * 10
        graph.addEdge(id, randVertex, weight)

        vertices += id

    }
    

    for (i <- N until S) {

        // select two different random vertices
        val rand = Random.shuffle(vertices.toList)
        val v1 = rand.head
        val v2 = rand.tail.head

        // connect them
        // TODO check edge does not exists
        val weight: Double = java.lang.Math.random() * 10
        graph.addEdge(v1, v2, weight)

    }
    
    val ft = graph.getDistanceTable(id)
    println("Reaching relaxation took: " + (System.currentTimeMillis - start).toString)

}

