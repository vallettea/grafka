Grafka is a scala library based on akka actor system that enables on-line computation on big graphs. 

Usual libraries like (networkx, igraph for python or even graphx, cassovary for scala) are aimed to make computation on a given graph. Here the computation (of all shortest paths for example) starts as soon as the graph is initialized and continues as the graph evolves.
A graph is thus a living thing adapting to change in its topolgy and working on itself to be ready for queries such as what is the shortest path between two edges.


## Quick start

```
import grafka

// we want all pairs shortest paths and pageRank on the graph
val graph = grafka.Graph(shortestPaths = True, pageRank = True)

val v1 = graph.addVertex(1)
val v2 = graph.addVertex(2)

//adding an edge ow weight 10.3
graph.addEdge(v1, v2, 10.3)
```

## Shortest paths

In order to model a city, we need to be able to move from one node of the network to another using the shortest path but:

- path can be closed (a metro line)
- routes can change during time (accident blocking)
- different metrics can be used for the routing (time, comfort etc)

So a shortest path algorithms should not be a function taking a graph as input and outputing a list of hops. Rather the graph itself should have shortest path information in its structure: each node should be able to forward a mover to the best next node for him to reach is final destination.

### Asynchronous Bellman-Ford

Description of the algo.

### Current state

The algo works:

```
make test
```

will make two basic tests, the second giving the shortests distance to all other graph.

```
make example
```

will run main in the example project which is computing all shortest paths on a random graph.
The dummy version of Bellman-Ford runs in `O(N*S)` where N is the number of node and S the number of edges.
This is not very good (will soon be enhanced) but two arguments play in favor of such an algo:

- once the graph is relaxed, all queries are very very fast (estimating a hashmap a few times) and if there is a change in the graph this changed propagates radially around this change which means that a user starting its journey in an unrelaxed graph goes in roughtly the good direction and finally reaches this propagating updated zone where the routing is correct.
- (I had it mind but right now i can't remember)

## PageRank

### To implement



