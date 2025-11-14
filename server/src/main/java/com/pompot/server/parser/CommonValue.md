# CommonValue

Summarizes a value that appears multiple times across parsed pom files. Each
entry records the `category` (for instance `property` or `dependency`), an
`identifier` describing the subject of the value and the repeated `value`
itself. The `occurrences` count indicates how many edges contributed to the
summary.

```
record CommonValue {
  String category
  String identifier
  String value
  int occurrences
}
```
