# GraphValue

Represents the payload stored by an edge. Values can either wrap a shared
`TextReference` or expose a nested structure composed of other `GraphValue`
instances. Use `GraphValue.text(reference)` when a simple textual payload is
required and `GraphValue.composite(children)` to associate named entries with
their own values.

```
sealed interface GraphValue {
  Optional<TextReference> text()
  Map<String, GraphValue> children()
}
```
