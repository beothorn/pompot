# Pompot Wiki

Welcome to pompot.

Pompot is a Maven projects manager that orchestrates work across multiple repositories so teams can reason about their entire ecosystem at once. This wiki repeats the core behaviors documented in code and tests so every reader can confirm what the tool already provides today.

## Current features

- **Dual execution modes**: the server starts in UI mode by default and exposes a REST API, while the CLI mode prints product information for headless workflows.
- **pom.xml bootstrapping**: Pompot scans the working directory by default or the folder provided via `--parent=<path>`, parsing every descendant `pom.xml` into an in-memory JSON snapshot (leading `~/` is expanded to the user home before scanning).
- **HTTP access to parsed metadata**: UI mode serves `GET /api/pom`, returning the stored pom snapshot or `404` when no project has been parsed yet.
- **Default Spring Boot server**: UI mode binds to port `9754`, making it easy to run the UI alongside other services.
- **Common value spotlight**: repeated property values and dependency versions are aggregated across parsed graphs and displayed at the top of the client UI so teams can identify coordinated upgrades quickly.

## Documentation map

- [Usage guide](usage/EntryPoint.md) — covers startup parameters, modes and HTTP workflows.
- [Technical reference](technical/EntryPoint.md) — documents the server components that implement parsing and storage.

Each section echoes the runtime behavior that the code already enforces so maintainers can cross-reference intent quickly.
