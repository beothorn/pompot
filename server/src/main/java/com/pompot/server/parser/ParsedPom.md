# ParsedPom

Simple data record describing a parsed pom file and associated metadata.

## Fields

- `String pomPath` – Absolute path to the parsed `pom.xml` file.
- `String relativePath` – Path to the pom relative to the scanned root directory.
- `String groupId` – Maven group identifier resolved for the pom.
- `String artifactId` – Maven artifact identifier resolved for the pom.
- `JsonNode model` – JSON tree produced from the Maven model.
