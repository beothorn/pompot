# CLI report internals

This page maps the `--report-common-values` flow back to the classes that make
it run. The implementation mirrors ticket [#10](../../tickets/10_cli_common_values_report.md)
and the behaviour covered by
[`CommonValueReportCommandTest`](../../server/src/test/java/com/pompot/server/cli/CommonValueReportCommandTest.md).

## Argument interception

`PompotApplication.main` inspects the raw argument array before mode detection.
If it finds either `--report-common-values=<dir>` or the split form
`--report-common-values <dir>`, it calls `runCommonValueReport` and skips the UI
and CLI mode handlers. Empty or missing directory arguments propagate as empty
strings so the command can surface a validation error consistently.

## Command composition

`runCommonValueReport` builds the components used by the headless command:

1. `PomFileParser` wraps Maven's `DefaultModelReader` and Jackson `ObjectMapper`
   to load each `pom.xml` into a `PomParseResult` (JSON model plus dependency graph).
2. `PomDirectoryScanner` walks the provided directory, normalises the root path
   and reuses the parser for every discovered `pom.xml`. It returns a
   `ScanResult` that records whether any files were found and stores the
   `ParsedPom` list sorted by group and artifact identifiers.
3. `CommonValueExtractor` iterates each parsed graph collecting repeated values
   across categories (`property`, `dependency`, `managed dependency`, `bom`,
   `parent`, `plugin`, `managed plugin` and `tile`). Identifiers are derived from
   graph node IDs, group/artifact pairs or scope/classifier suffixes so the table
   remains human-readable.
4. `CommonValueReportCommand` validates the directory, expands leading `~/`,
   delegates to the scanner and extractor, then prints a column-aligned table to
   the provided `PrintStream`s.

## Output guarantees

- Validation errors use exit code `1` and always describe the failing path on
  standard error.
- Successful scans use exit code `0`. When no repeats exist, the command prints
  an explanatory sentence and leaves error output empty so automation can treat
  the run as successful.
- Repeated values are sorted case-insensitively by category, identifier and
  value, matching the ordering asserted in the unit tests and ensuring stable
  diffs in downstream tooling.
