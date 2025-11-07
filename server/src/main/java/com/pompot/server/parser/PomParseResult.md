# PomParseResult

Encapsulates the outcome of parsing a single `pom.xml` file.

## Fields

- `String groupId` – Group identifier resolved for the project, empty when unavailable.
- `String artifactId` – Artifact identifier resolved for the project, empty when unavailable.
- `JsonNode model` – JSON tree built from the Maven model.
