# Text

Immutable record that wraps the raw string stored in the graph. It always
normalizes `null` values to the empty string so callers do not need to handle
`null` checks when reading the graph content.

```
record Text {
  constructor(String value) {
    value = value or ""
  }
}
```
