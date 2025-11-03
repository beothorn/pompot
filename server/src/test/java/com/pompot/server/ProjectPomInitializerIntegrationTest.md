# ProjectPomInitializerIntegrationTest

Ensures the Spring application stores a parsed pom when the `--project` argument is provided.

## storesParsedPomDuringStartup
- Effect: Starts the Spring context with `--project=src/test/resources/projects/simple` and asserts the repository contains a parsed pom.
