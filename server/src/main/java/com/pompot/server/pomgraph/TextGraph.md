# TextGraph

Owns the set of `GraphNode` instances and every `TextReference` attached to the
edges. Nodes can be created on demand and text references are centrally managed
so they can be shared by multiple edges.

```
class TextGraph {
  constructor()
  constructor(TextGraph source)
  GraphNode addNode(String id)
  Optional<GraphNode> findNode(String id)
  Collection<GraphNode> nodes()
  TextReference createText(String value)
  Collection<TextReference> texts()
  TextGraph copy()
}
```
