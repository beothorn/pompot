# ProjectPomInitializer

Loads every `pom.xml` file found under the working directory (or `--parent` argument) when the application boots in UI mode.

## Constructor

### Parameters
- `ParsedPomRepository parsedPomRepository` – Stores the parsed results for later retrieval.
- `CommonValueExtractor commonValueExtractor` – Aggregates repeated values across parsed graphs.
- `PomDirectoryScanner pomDirectoryScanner` – Locates and parses pom files under the configured root.

## run

### Parameters
- `ApplicationArguments arguments` – Command line arguments provided to Spring Boot.

### Pseudocode
```
scanRoot = resolve --parent argument or working directory
if scanRoot invalid:
  clear repository and return
if --parent starts with '~':
  expand it to the user home directory before resolving the path
scanResult = pomDirectoryScanner.scan(scanRoot)
if no pom files found:
  clear repository and return
if scanResult contains no parsed entries:
  clear repository and return
parsedPoms = scanResult.parsedPoms
commonValues = extract repeated values from parsed entries
store collection with entries and commonValues in repository using absolute scan root
log how many pom files were parsed
```
