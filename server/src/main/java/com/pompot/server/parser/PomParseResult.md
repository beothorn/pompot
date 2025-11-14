# PomParseResult

Encapsulates the outcome of parsing a single `pom.xml` file. Besides the JSON
representation, it now exposes the `TextGraph` structure that mirrors the pom in
an editable form. Consumers receive a defensive copy of the graph so the parser's
internal state is never exposed directly while the `TextReference` handles remain
shared across copies.

```
record PomParseResult {
  String groupId
  String artifactId
  JsonNode model
  TextGraph graph
}
```
