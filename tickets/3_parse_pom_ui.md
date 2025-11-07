# Task number
3
# What client asked
Parse every `pom.xml` found under a workspace (defaulting to the execution directory or provided via `--parent`) and expose the results through the UI in a readable format.
# Technical solution
Added Maven's model builder dependency to the server, introduced a parser component that converts each pom model to JSON with metadata, stored the collection in an in-memory repository, exposed a REST endpoint, and updated the React client to fetch and render the structured data.
# What changed
- Added backend components for parsing and serving pom collections.
- Extended the client UI to request grouped pom data and display each file through a collapsible YAML-like view.
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
