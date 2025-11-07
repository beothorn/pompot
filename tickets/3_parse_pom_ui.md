# Task number
3
# What client asked
Parse the pom.xml from a project specified with `--project` and expose it through the UI in a readable format.
# Technical solution
Added Maven's model builder dependency to the server, introduced a parser component that converts the pom model to JSON, stored it in an in-memory repository, exposed a REST endpoint, and updated the React client to fetch and render the structured data.
# What changed
- Added backend components for parsing and serving pom information.  
- Extended the client UI to request the parsed pom and display it as a YAML-like view.  
- Added unit tests for the parser and the startup initializer.  
# Required Checklist !!!!
[] Test coverage is higher than 80% by running ./run-tests.sh  
[x] No compile warnings or errors added.  
[x] Security issues checked, listed or mitigated.  
[] Spotbugs ran, no new issues  
[] Feature is described on the wiki  
[x] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.  
# Notes
Executed `mvn test` for the server module to cover the new backend components.
