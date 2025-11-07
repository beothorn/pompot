# Task number
2

# What client asked
Bootstrap the project to deliver a simple hello world experience with Maven on the backend and webpack on the frontend. Provide a script that outputs a single runnable jar with both UI and CLI modes and set up CI if possible.

# Technical solution
Create a Spring Boot application packaged with Maven that can either serve the UI or print a CLI about message depending on the `--mode` flag. Build a React frontend bundled with webpack and copy the assets into the jar during the build script. Automate the bundle-and-package steps through `build/build.sh` and exercise the client and server builds on GitHub Actions.

# What changed
- Added a webpack + React client that renders a hello world view.  
- Added a Spring Boot server that prints an about message in CLI mode or serves the bundled UI on port 9754.  
- Added a build script to package the client assets and produce a single executable jar under `build/output/`.  
- Added a CI workflow to build both modules and run the tests.  

# Required Checklist !!!!
[] Test coverage is higher than 80% by running ./run-tests.sh  
[x] No compile warnings or errors added.  
[x] Security issues checked, listed or mitigated.  
[] Spotbugs ran, no new issues  
[] Feature is described on the wiki  
[x] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.  

# Notes
The hello world stack focuses on showcasing the runtime entrypoints and does not yet provide automated quality gates beyond the GitHub Actions workflow.
