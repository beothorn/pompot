# PomParseResult

Encapsulates the outcome of parsing a single `pom.xml` file. Besides the JSON
representation, it now exposes the `TextGraph` structure that mirrors the pom in
an editable form.

```
record PomParseResult {
  String groupId
  String artifactId
  JsonNode model
  TextGraph graph
}
```
