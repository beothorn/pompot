# ParsedPomRepository

Thread-safe in-memory store for the parsed pom scan information.

## fetch

### Returns
- `Optional<ParsedPomCollection>` – Latest parsed pom inventory if available.

## store

### Parameters
- `ParsedPomCollection parsedPomCollection` – Value to persist in memory.

Replaces any previously stored data.

## clear

Removes any stored parsed pom data.
