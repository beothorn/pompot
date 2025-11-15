# `--report-common-values` CLI report

The common values report provides the same repeated-value spotlight that the UI
shows, but renders it as a terminal table so teams can script the workflow.
Ticket [#10](../../tickets/10_cli_common_values_report.md) introduced the flag and
its behaviour is enforced by [`CommonValueReportCommandTest`](../../server/src/test/java/com/pompot/server/cli/CommonValueReportCommandTest.md).

## When to use it

Use the report when you need a quick inventory of values that appear multiple
times across a workspace without starting the HTTP server. It traverses every
`pom.xml` under the provided directory, aggregates duplicates and prints the
results to standard output.

## Invoking the report

Run the application with one of the following syntaxes:

```bash
java -jar pompot.jar --report-common-values=/path/to/workspace
java -jar pompot.jar --report-common-values /path/to/workspace
```

- The directory argument is mandatory. When it is missing or blank the command
  prints `--report-common-values requires a directory argument.` and returns
  exit code `1`.
- Leading `~/` is expanded to the current user's home directory before scanning.
  Values such as `~/projects` therefore behave the same as their absolute
  counterparts.
- The path must resolve to an existing directory. Invalid paths trigger
  `Directory not found: <path>` on standard error together with exit code `1`.

## What the report prints

1. A heading that states the normalized root directory.
2. A table with four columns when repeated values are detected:
   - **Category** – logical group such as `property`, `dependency`, `bom`,
     `plugin`, `managed plugin`, `managed dependency`, `parent` or `tile`.
   - **Identifier** – human-friendly locator derived from group/artifact pairs,
     property names or tile IDs.
   - **Value** – the repeated text (property value, version, coordinate, etc.).
   - **Occurrences** – number of times the value appeared across all parsed poms.
3. A trailing newline so shells can continue piping the output.

When the scan succeeds but no duplicates are present, standard output contains
one of the following messages:

- `No repeated values were detected under <path>` – duplicates were not found.
- `No pom.xml files were found under <path>` – traversal succeeded but there are
  no Maven projects under the directory.

Both cases produce exit code `0`, making them script-friendly.

## Exit codes

| Exit code | Meaning |
| --- | --- |
| `0` | The directory was valid and the scan finished (even if no duplicates were found). |
| `1` | Input validation failed (missing argument, invalid path or unreadable directory). |

## Related features

- The UI uses the same aggregation logic via `CommonValueExtractor`, so the
  report mirrors what operators see in the browser.
- Technical details about the scanner and extractor are documented in
  [CLI report internals](../technical/CommonValueReport.md).
