# Task number
5

# What client asked
Fix the application startup failure caused by infinite recursion when parsing pom files and add regression tests.

# Technical solution
Introduce a sanitization pass that clears parent pointers from Xpp3Dom nodes before Jackson serialization, catch unexpected runtime failures, and add a regression pom fixture plus assertions to cover plugin and reporting configurations.

# What changed
- Added recursive sanitization methods to PomFileParser to break Xpp3Dom parent cycles and guard runtime serialization failures.
- Documented the sanitization step and extended unit tests with a plugin-configuration scenario.
- Added a dedicated test pom in resources mirroring plugin and reporting configurations that previously crashed the app.

# Required Checklist !!!!
[ ] Test coverage is higher than 80% by running ./run-tests.sh
[x] No compile warnings or errors added.
[x] Security issues checked, listed or mitigated.
[ ] Spotbugs ran, no new issues
[ ] Feature is described on the wiki
[x] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.

# Notes
Runtime exceptions are logged and converted to Optional.empty() as an additional safety net even though sanitization should prevent the recursion from occurring.
