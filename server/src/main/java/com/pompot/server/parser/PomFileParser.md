# PomFileParser

Parses a Maven `pom.xml` file located inside a project directory and converts it to a JSON tree.

## Constructor

### Parameters
- `ModelReader modelReader`
- `ObjectMapper objectMapper`

Allows injecting a custom reader (primarily for testing). The mapper is defensively copied so
later mutations from outside do not affect parser behavior.

## parse

### Parameters
- `Path projectRoot` – Directory that should contain the `pom.xml`.

### Returns
- `Optional<PomParseResult>` – Present when parsing was successful.

### Pseudocode
```
if projectRoot is null:
  return empty optional
pomLocation = projectRoot.resolve("pom.xml")
if pomLocation is not a regular file:
  log warning and return empty optional
try reading model using Maven's ModelReader with non-strict mode
remove parent pointers from any Xpp3Dom configurations to avoid recursion
convert model to JsonNode via ObjectMapper
derive groupId and artifactId (fallback to parent when absent)
build TextGraph representation describing pom relationships
return optional containing PomParseResult with metadata, JsonNode and graph
catch IOException or runtime serialization errors:
  log error and return empty optional
```
