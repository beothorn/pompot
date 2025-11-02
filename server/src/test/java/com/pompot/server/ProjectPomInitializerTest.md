# ProjectPomInitializerTest

Validates how the initializer reacts to different command line inputs.

## setUp

Creates fresh instances of the parser, repository and initializer before each test.

## storesParsedPomWhenProjectArgumentIsPresent

### Behavior
- Runs the initializer with a valid `--project` argument pointing to the sample project.
- Asserts the repository stores a parsed pom afterwards.

## clearsRepositoryWhenArgumentMissing

### Behavior
- Pre-populates the repository with placeholder data.
- Runs the initializer without the `--project` argument.
- Asserts the repository ends up empty.
