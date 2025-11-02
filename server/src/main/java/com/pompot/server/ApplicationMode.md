# ApplicationMode

Represents the startup mode requested by the user. Modes:
- `UI` to run the Spring Boot web server and serve the browser UI.
- `CLI` to print the command line about message and exit.

## fromArguments
```
ApplicationMode fromArguments(String[] arguments)
```
Scans the command-line arguments and returns `CLI` when `--mode=cli` is present; otherwise returns `UI`.

## isCli
```
boolean isCli()
```
Returns true when the instance equals `CLI`.
