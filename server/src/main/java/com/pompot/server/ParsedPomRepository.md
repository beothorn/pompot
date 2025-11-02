# ParsedPomRepository

Thread-safe in-memory store for the parsed pom information.

## fetch

### Returns
- `Optional<ParsedPom>` – Latest parsed pom if available.

## store

### Parameters
- `ParsedPom parsedPom` – Value to persist in memory.

Replaces any previously stored data.

## clear

Removes any stored parsed pom data.
