# ProjectPomController

REST endpoint that exposes parsed pom inventory to the client application.

## Constructor

### Parameters
- `ParsedPomRepository parsedPomRepository` – Source of parsed pom data.

## fetchParsedPom

### Returns
- `ResponseEntity<ParsedPomCollection>` – HTTP 200 with parsed pom data or HTTP 404 when nothing was loaded.

### Pseudocode
```
retrieve parsed pom collection from repository
if empty:
  return 404 response
return 200 response containing the collection
```
