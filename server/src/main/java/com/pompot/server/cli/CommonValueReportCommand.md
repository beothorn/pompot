# CommonValueReportCommand

Executes the `--report-common-values` CLI option. It validates the provided
folder, uses `PomDirectoryScanner` to parse all pom files and prints a table
with the repeated values returned by `CommonValueExtractor`. When no repeated
values exist it prints a short explanatory message so scripts can still treat
it as a successful run.
