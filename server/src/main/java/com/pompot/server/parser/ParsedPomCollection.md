# ParsedPomCollection

Describes the outcome of scanning a directory for pom files.

## Fields

- `String scannedRoot` – Absolute directory that served as the scan root.
- `List<ParsedPom>` – Parsed entries discovered under the root.

## Behavior

- Copies the provided list of entries to prevent external mutations from affecting the stored collection.
