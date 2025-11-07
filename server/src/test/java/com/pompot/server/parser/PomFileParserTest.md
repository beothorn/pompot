# PomFileParserTest

Ensures the pom parser converts a pom file into a JSON tree.

## parsesPomIntoJsonTree

### Behavior
- Loads the sample project under `src/test/resources/projects/simple`.
- Parses the pom file using `PomFileParser`.
- Verifies the resulting JSON node contains the expected `groupId` and `artifactId` values.

## parsesPomWithPluginConfigurationWithoutRecursing

### Behavior
- Loads the project at `src/test/resources/projects/with-plugin-config` which declares plugin and reporting configurations.
- Parses the pom using `PomFileParser` after the recursive-parent sanitization step.
- Asserts the JSON tree contains the plugin configuration entries and the reporting configuration, demonstrating serialization completed.
