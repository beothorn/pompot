# Task number
7

# What client asked
Add SpotBugs to the backend, ESLint to the client, fix any real issues the analyzers report, and wire both into the build.

# Technical solution
- Add ESLint configuration, dependencies, ignore list, and npm script for the client so TypeScript sources can be linted.
- Introduce the SpotBugs Maven plugin to the server build so static analysis runs during `verify`.
- Address the new SpotBugs finding in `PomFileParser` by defensively copying the injected `ObjectMapper`.
- Update the build script to run the new lint step and rely on `mvn clean verify` so SpotBugs executes with the build.

# What changed
- ESLint configuration files and dependencies were added to the client along with an npm `lint` script.
- SpotBugs is now configured for the server module and the static analysis warning in `PomFileParser` was resolved.
- `build/build.sh` now runs the client lint prior to building and promotes the server build to `mvn clean verify` so SpotBugs runs automatically.

# Required Checklist !!!!
[] Test coverage is higher than 80% by running ./run-tests.sh
[x] No compile warnings or errors added.
[] Security issues checked, listed or mitigated.
[x] Spotbugs ran, no new issues
[] Feature is described on the wiki
[x] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.

# Notes
`npm run lint` and `mvn -B clean verify` were executed locally to confirm the new analyzers succeed.
