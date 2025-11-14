# Task number
9
# What client asked
Highlight common property and dependency versions across parsed poms and display them prominently in the UI. Update the graph data model so edges can hold more detailed payloads.
# Technical solution
Introduce a `GraphValue` payload abstraction for `GraphEdge`, allowing both shared text references and composite structures. Build a `CommonValueExtractor` to aggregate repeated values from parsed graphs and expose them through the API. Update the client to render a common values section at the top of the overview page.
# What changed
- Expanded graph model to use `GraphValue` payloads and adjusted parser/tests accordingly.
- Added common value aggregation, documentation, and unit tests.
- Surfaced aggregated values in the API and rendered them in the client UI with accompanying tests.
# Required Checklist !!!!
[] Test coverage is higher than 80% by running ./run-tests.sh
[x] No compile warnings or errors added.
[x] Security issues checked, listed or mitigated.
[] Spotbugs ran, no new issues
[x] Feature is described on the wiki
[x] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.
# Notes
Server and client unit tests were executed individually (`mvn -f server/pom.xml test` and `npm test -- --runInBand`).
