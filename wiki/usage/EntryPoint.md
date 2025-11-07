# Usage guide

The steps below restate what the server already performs so operators can follow the same flows.

## Launching the application

1. Build or download `pompot.jar`.
2. Run UI mode (default) to serve the REST API:
   ```bash
   java -jar pompot.jar --project=/path/to/project
   ```
   - The `--project` flag points to the directory containing `pom.xml`.
   - UI mode binds to port `9754`, matching the `DEFAULT_PORT` constant in `PompotApplication`.
3. Run CLI mode when you only need the product banner:
   ```bash
   java -jar pompot.jar --mode=cli
   ```
   - CLI mode skips Spring Boot initialization and prints `pompot <version> - workspace manager prototype`.

## Parsing behavior

- When `--project` is omitted or empty, the initializer clears the stored snapshot and the controller will respond with `404`.
- When `--project` points to an invalid path, the application logs an error and also clears the stored snapshot.
- On success the parser produces a JSON representation of the Maven model and stores it alongside the absolute project root path.

## Retrieving the parsed pom

1. Start the application in UI mode with a valid `--project` argument.
2. Issue an HTTP request:
   ```bash
   curl http://localhost:9754/api/pom
   ```
3. Expected responses:
   - `200 OK` with the JSON snapshot when parsing succeeded.
   - `404 Not Found` when no parsed pom is available (for example, due to invalid input or because the application ran in CLI mode).

These outcomes duplicate the repository and controller behavior so operators can immediately understand the results they receive.
