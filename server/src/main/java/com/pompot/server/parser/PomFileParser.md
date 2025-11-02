# PomFileParser

Parses a Maven `pom.xml` file located inside a project directory and converts it to a JSON tree.

## Constructor

### Parameters
- `ObjectMapper objectMapper` – Used to transform the Maven model into a JSON tree.

Creates an instance backed by Maven's `DefaultModelReader`.

## Constructor

### Parameters
- `ModelReader modelReader`
- `ObjectMapper objectMapper`

Allows injecting a custom reader (primarily for testing).

## parse

### Parameters
- `Path projectRoot` – Directory that should contain the `pom.xml`.

### Returns
- `Optional<JsonNode>` – Present when parsing was successful.

### Pseudocode
```
if projectRoot is null:
  return empty optional
pomLocation = projectRoot.resolve("pom.xml")
if pomLocation is not a regular file:
  log warning and return empty optional
try reading model using Maven's ModelReader with non-strict mode
convert model to JsonNode via ObjectMapper
return optional containing JsonNode
catch IOException or ModelParseException:
  log error and return empty optional
```
