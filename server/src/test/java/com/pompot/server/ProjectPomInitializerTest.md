# ProjectPomInitializerTest

Validates how the initializer reacts to different command line inputs.

## setUp

Creates fresh instances of the parser, repository and initializer before each test.

## storesParsedPomsWhenParentArgumentIsPresent

### Behavior
- Runs the initializer with a valid `--parent` argument pointing to the sample projects directory.
- Asserts the repository stores both parsed pom entries.

## clearsRepositoryWhenDirectoryHasNoPoms

### Behavior
- Seeds the repository with placeholder data.
- Runs the initializer against an empty temporary directory.
- Asserts the repository ends up empty when no pom files exist.
