# PompotApplication

Spring Boot entry point that wires the Pompot server.

## main
```
void main(String[] args)
```
Determines the requested application mode. When the CLI flag is present, it prints the about message and exits. Otherwise it boots the Spring application and serves the UI on port `9754`.

## printAbout
```
void printAbout()
```
Detects the packaged version and prints the about line `pompot <version> - workspace manager prototype` to standard output. Falls back to `development` when the jar metadata does not include a version.
