# Task number
8
# What client asked
Improve the client pom viewer so every entry is presented inside collapsible boxes with readable labels, show scalar fields as
inputs, and expose a `data-pompath` attribute that uniquely identifies each value for future editing work.
# Technical solution
Refactored the React client renderer to recursively map pom models into nested `<details>` sections with contextual summaries,
rendered primitives as read-only inputs (checkboxes, number and text fields) carrying the requested `data-pompath`, and updated
styling to highlight the box hierarchy. Added helpers to generate descriptive labels from coordinates, created singularized
labels for array items, and ensured each node builds a unique path based on its location.
# What changed
- Replaced the previous YAML-like text formatter with a recursive renderer that outputs collapsible boxes and read-only inputs.
- Styled the pom viewer to display nested sections with improved spacing and readability.
- Adjusted client tests to assert the new structure and verify `data-pompath` attributes for key fields.
# Required Checklist !!!!
[] Test coverage is higher than 80% by running ./run-tests.sh
[x] No compile warnings or errors added.
[x] Security issues checked, listed or mitigated.
[] Build script with static analysis ran, no new issues
[] Feature is described on the wiki
[x] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.
# Notes
Executed `npm test -- --runInBand` inside the `client` workspace to cover the updated UI behavior.
