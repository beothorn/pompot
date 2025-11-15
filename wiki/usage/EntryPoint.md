# Usage guide

The steps below restate what the server already performs so operators can follow the same flows.

## Launching the application

1. Build or download `pompot.jar`.
2. Run UI mode (default) to serve the REST API:
   ```bash
   java -jar pompot.jar
   ```
   - Pompot scans the current working directory recursively.
   - Provide `--parent=/path/to/workspace` to scan a different directory while keeping UI mode enabled. Paths starting with `~/` are expanded to the user home before scanning.
   - UI mode binds to port `9754`, matching the `DEFAULT_PORT` constant in `PompotApplication`.
3. Run CLI mode when you only need the product banner:
   ```bash
   java -jar pompot.jar --mode=cli
   ```
   - CLI mode skips Spring Boot initialization and prints `pompot <version> - workspace manager prototype`.

## Parsing behavior

- When `--parent` is omitted Pompot scans the working directory and stores every descendant `pom.xml` that parses successfully.
- When `--parent` is empty or invalid, the application logs an error and clears any previously stored data.
- When `--parent` begins with `~/`, Pompot expands it to the home directory and continues with the resolved absolute path.
- On success the parser produces JSON representations of the Maven models together with their file metadata.

## Retrieving the parsed pom

1. Start the application in UI mode from the target workspace or supply `--parent=/path/to/workspace` (the same `~/` expansion applies here).
2. Issue an HTTP request:
   ```bash
   curl http://localhost:9754/api/pom
   ```
3. Expected responses:
   - `200 OK` with the JSON snapshot when parsing succeeded.
   - `404 Not Found` when no parsed pom is available (for example, due to invalid input or because the application ran in CLI mode).

These outcomes duplicate the repository and controller behavior so operators can immediately understand the results they receive.

## Reporting repeated values

Run `java -jar pompot.jar --report-common-values /path/to/workspace` to print the repeated-value table described in [the CLI report guide](CommonValueReport.md). The command scans the provided directory recursively, expands leading `~/` and exits with code `0` when the scan succeeds even if no duplicates exist. Input validation errors are written to standard error with exit code `1`.
