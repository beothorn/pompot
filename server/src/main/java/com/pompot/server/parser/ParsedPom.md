# ParsedPom

Simple data record describing a parsed pom file and associated metadata. It now
includes the `TextGraph` representation so callers can inspect or edit the pom
structure through shared textual references. The graph is stored and returned as
a defensive copy to avoid leaking the internal repository state while still
sharing the mutable `TextReference` handles.

```
record ParsedPom {
  String pomPath
  String relativePath
  String groupId
  String artifactId
  JsonNode model
  TextGraph graph
}
```
