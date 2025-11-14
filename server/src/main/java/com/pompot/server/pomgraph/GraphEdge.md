# GraphEdge

Represents a directed connection between two nodes. Each edge stores the
relationship name and a `TextReference` payload so multiple edges can reuse the
same textual value.

```
record GraphEdge {
  GraphNode source
  GraphNode target
  String relationship
  TextReference value
}
```
