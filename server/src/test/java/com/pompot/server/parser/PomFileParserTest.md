# PomFileParserTest

Ensures the pom parser converts a pom file into a JSON tree.

## parsesPomIntoJsonTree

### Behavior
- Loads the sample project under `src/test/resources/projects/simple`.
- Parses the pom file using `PomFileParser`.
- Verifies the resulting JSON node contains the expected `groupId` and `artifactId` values.
