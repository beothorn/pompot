# Technical reference

This section mirrors the behaviors encoded in the server so maintainers can map runtime effects back to source files.

## Runtime overview

1. **Application bootstrap** (`server/src/main/java/com/pompot/server/PompotApplication.java`)
   - `PompotApplication.main` reads command-line arguments via `ApplicationMode.fromArguments`.
   - UI mode loads Spring Boot and pins the HTTP server to port `9754` (`DEFAULT_PORT`).
   - CLI mode short-circuits the Spring context and prints the same "about" message as the binary's standard output tests expect.
2. **Mode detection** (`server/src/main/java/com/pompot/server/arguments/ApplicationMode.java`)
   - `fromArguments` scans for `--mode=cli`; absence means UI mode.
   - `isCli` centralizes the CLI check so callers do not reimplement comparisons.
3. **Startup parsing** (`server/src/main/java/com/pompot/server/ProjectPomInitializer.java`)
   - Implements `ApplicationRunner` so it executes after the Spring context is ready in UI mode.
   - Resolves the scan root from `--parent` or defaults to the working directory, recursively discovering every `pom.xml`.
   - Stores a `ParsedPomCollection` with metadata for each parsed file or clears the repository when traversal or parsing fails.
4. **pom.xml parser** (`server/src/main/java/com/pompot/server/parser/PomFileParser.java`)
   - Wraps Maven's `ModelReader` to read the `pom.xml` files selected by the initializer.
   - Converts each `Model` to JSON through Jackson while scrubbing recursive `Xpp3Dom` parent references so serialization terminates.
   - Returns `Optional.empty()` for missing files, invalid paths or runtime exceptions, keeping error handling consistent across callers.
5. **In-memory storage** (`server/src/main/java/com/pompot/server/parser/ParsedPomRepository.java`)
   - Uses an `AtomicReference` to store the latest `ParsedPomCollection`.
   - `fetch`, `store` and `clear` wrap direct mutations to make concurrent controller access predictable.
6. **HTTP controller** (`server/src/main/java/com/pompot/server/ProjectPomController.java`)
   - Exposes `GET /api/pom`, returning `200` with the stored collection or `404` when the repository is empty.
   - Provides the UI with a stable contract that mirrors the repository semantics.

## Data flow summary

```
CLI args --> ApplicationMode --> PompotApplication
                     |                |
                     |                +--> Spring Boot (UI mode) on port 9754
                     v
            ProjectPomInitializer
                     |
                     v
              PomFileParser (ModelReader + ObjectMapper)
                     |
                     v
            ParsedPomRepository --(GET /api/pom)--> ProjectPomController
```

The diagram repeats the same flow enforced in code so readers can cross-check expectations without opening the source.
