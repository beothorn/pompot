# GraphNode

Stores the outgoing relationships from a graph vertex. Edges are grouped by the
relationship name so callers can inspect all connections or just a subset.

```
class GraphNode {
  constructor(String id)
  String id()
  GraphEdge connect(String relationship, GraphNode target, TextReference value)
  GraphEdge connect(String relationship, GraphNode target, GraphValue value)
  Collection<GraphEdge> edges()
  Collection<GraphEdge> edges(String relationship)
}
```
