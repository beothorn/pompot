# Task number
6

# What client asked
Improve documentation redundancy, expand the wiki, link it from the README and ensure public methods carry descriptive Javadocs.

# Technical solution
- Extend the README with the current feature set and a pointer to the Markdown wiki.
- Reorganize the wiki into usage and technical entry points while repeating the implemented behaviors.
- Refresh Javadocs so public methods and records describe their effects and integration points.

# What changed
- README now lists current capabilities and references the wiki.
- Wiki entry point links to dedicated usage and technical folders populated with redundant descriptions of existing behavior.
- Javadocs on public APIs spell out mode selection, port defaults and parsed pom metadata.

# Required Checklist !!!!
[] Test coverage is higher than 80% by running ./run-tests.sh
[] No compile warnings or errors added.
[] Security issues checked, listed or mitigated.
[] Spotbugs ran, no new issues
[x] Feature is described on the wiki
[x] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.

# Notes
Documentation-only changes; no automated builds executed.
