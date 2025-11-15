# 10
Add CLI command to report repeated pom values

# What client asked
Add a command line option that given the folder it prints a report of the repeated (identical) values for properties, dependencies, boms, pom parent, plug-in, tiles, etc.

# Technical solution
- Extend the existing parsing pipeline so BOM, parent, plugin, managed plugin and tile relationships are captured in the graph output.
- Build a reusable directory scanner that walks a folder, parses every pom.xml using PomFileParser and returns the parsed entries.
- Introduce a CLI command that validates the provided directory, runs the scanner and uses CommonValueExtractor to compute repeated values, then renders a text table.
- Wire the CLI flag into PompotApplication so `--report-common-values <dir>` triggers the new command before normal mode detection.

# What changed
- Added the new CLI command, directory scanner, parser enhancements, Spring wiring and unit tests.
- Updated ProjectPomInitializer to reuse the shared scanner.
- Documented the new components and wrote fixture pom files for CLI tests.

# Required Checklist !!!!
[x] Test coverage is higher than 80% by running ./run-tests.sh
[x] No compile warnings or errors added.
[x] Security issues checked, listed or mitigated.
[x] Build script with static analysis ran, no new issues
[x] Feature is described on the wiki
[x] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.

# Notes
Maven unit tests cover the new CLI command and extractor behaviour. The CLI option currently surfaces results to STDOUT.
