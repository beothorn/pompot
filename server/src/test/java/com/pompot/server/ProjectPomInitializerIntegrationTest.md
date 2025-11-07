# ProjectPomInitializerIntegrationTest

Ensures the Spring application stores parsed pom data when the `--parent` argument is provided.

## storesParsedPomDuringStartup
- Effect: Starts the Spring context with `--parent=src/test/resources/projects` and asserts the repository contains parsed poms.
