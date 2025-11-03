# ParserConfiguration

Provides Spring beans required by the parser package.

## modelReader
- Returns: Maven `ModelReader` instance backed by `DefaultModelReader`.
- Responsibility: supplies the reader used by `PomFileParser`.

## pomFileParser
- Parameters: `ModelReader modelReader`, `ObjectMapper objectMapper`.
- Returns: Configured `PomFileParser` bound to the provided collaborators.
- Responsibility: exposes the parser as a Spring bean.
