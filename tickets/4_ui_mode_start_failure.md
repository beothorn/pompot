# Task number
4
# What client asked
Fix the UI mode startup failure that happens after running ./build.sh and ensure all classes have explanatory comments similar to ApplicationMode.
# Technical solution
Annotated the PomFileParser constructor for Spring injection to restore UI mode startup, reran the bundled build to verify the fix, and documented all server-side classes with JavaDoc summaries and method explanations.
# What changed
- Enabled Spring to instantiate PomFileParser by marking the ObjectMapper constructor for autowiring.
- Added JavaDoc comments to every server class so their responsibilities are explicit.
- Verified the full build script to ensure the UI jar boots without dependency errors.
# Required Checklist !!!!
[] Test coverage is higher than 80% by running ./run-tests.sh
[x] No compile warnings or errors added.
[x] Security issues checked, listed or mitigated.
[] Spotbugs ran, no new issues
[] Feature is described on the wiki
[x] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.
# Notes
Ran ./build/build.sh which executes the client build and `mvn clean package` for the server, covering unit tests and verifying boot.
