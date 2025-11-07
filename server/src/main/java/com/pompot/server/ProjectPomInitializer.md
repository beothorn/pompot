# ProjectPomInitializer

Loads every `pom.xml` file found under the working directory (or `--parent` argument) when the application boots in UI mode.

## Constructor

### Parameters
- `PomFileParser pomFileParser` – Reads pom files and converts them into JSON trees.
- `ParsedPomRepository parsedPomRepository` – Stores the parsed results for later retrieval.

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
pomFiles = recursively list pom.xml under scanRoot
  ignore entries without a valid file name
if no pomFiles:
  clear repository and return
for each pomFile:
  parse directory containing pom
  if parsing succeeds:
    derive relative path
    collect parsed entry with metadata
if no entries parsed:
  clear repository and return
sort entries by groupId, artifactId and relative path
store collection in repository with absolute scan root
log how many pom files were parsed
```
