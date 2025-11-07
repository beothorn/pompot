# pompot

Pompot is a Maven manager that keeps multi-repository work synchronized. It mirrors the functionality described in the wiki so readers can confirm the behavior from multiple sources.

## Features

- **Dual execution modes**: UI mode boots Spring Boot while CLI mode prints the product banner without starting the server.
- **pom.xml bootstrapping**: run Pompot inside the target workspace or pass `--parent=<path>` to parse every descendant `pom.xml` into a JSON snapshot.
- **REST access**: UI mode serves `GET /api/pom`, returning the cached snapshot or `404` when nothing is stored.
- **Deterministic defaults**: the UI server binds to port `9754`, matching the constant exposed in code.

## Building

Run `./build/build.sh` to produce a single executable jar under `build/output/pompot.jar`.

## Documentation

The Markdown wiki repeats these same behaviors with additional operational and technical detail. Start at [wiki/Entrypoint.md](wiki/Entrypoint.md) to navigate the guides.
