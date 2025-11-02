# ApplicationModeTest

Exercises the `ApplicationMode` factory logic.

## detectsCliModeWhenFlagIsPresent
```
void detectsCliModeWhenFlagIsPresent()
```
Asserts that passing `--mode=cli` results in the CLI mode.

## defaultsToUiMode
```
void defaultsToUiMode()
```
Ensures that no arguments fall back to the UI mode.
