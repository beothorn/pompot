# ProjectPomController

REST endpoint that exposes the parsed pom model to the client application.

## Constructor

### Parameters
- `ParsedPomRepository parsedPomRepository` – Source of parsed pom data.

## fetchParsedPom

### Returns
- `ResponseEntity<ParsedPom>` – HTTP 200 with parsed pom data or HTTP 404 when the pom was not loaded.

### Pseudocode
```
retrieve parsed pom from repository
if empty:
  return 404 response
return 200 response containing parsed pom data
```
