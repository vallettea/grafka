Grafka is a scala library based on akka actor system that enables on-line computation on big graphs. 

Usual libraries like (networkx, igraph for python or even graphx, cassovary for scala) are aimed to make computation on a given graph. Here the computation (of all shortest paths for example) starts as soon as the graph is initialized and continues as the graph evolves.


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


