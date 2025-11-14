# GraphEdge

Represents a directed connection between two nodes. Each edge stores the
relationship name and a `GraphValue` payload so entries can reuse text
references or expose nested structures when more context is required.

```
record GraphEdge {
  GraphNode source
  GraphNode target
  String relationship
  GraphValue value
}
```
