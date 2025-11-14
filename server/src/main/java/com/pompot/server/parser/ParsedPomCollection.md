# ParsedPomCollection

Describes the outcome of scanning a directory for pom files.

## Fields

- `String scannedRoot` – Absolute directory that served as the scan root.
- `List<ParsedPom>` – Parsed entries discovered under the root.
- `List<CommonValue>` – Aggregated values shared across the parsed graphs.

## Behavior

- Copies the provided lists to prevent external mutations from affecting the stored collection.
