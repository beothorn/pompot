# ProjectPomInitializer

Loads the project's pom file when the application boots in UI mode.

## Constructor

### Parameters
- `PomFileParser pomFileParser` – Reads the pom file and converts it into a JSON tree.
- `ParsedPomRepository parsedPomRepository` – Stores the parsed result for later retrieval.

## run

### Parameters
- `ApplicationArguments arguments` – Command line arguments provided to Spring Boot.

### Pseudocode
```
if --project argument is missing:
  clear repository and return
fetch first value of --project argument
if value is absent or invalid path:
  clear repository and return
parse pom using PomFileParser
if parsing failed:
  clear repository and return
store parsed pom (with absolute path) in repository
log that parsing succeeded
```
