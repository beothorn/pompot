package com.pompot.server.cli;

import com.pompot.server.parser.CommonValue;
import com.pompot.server.parser.CommonValueExtractor;
import com.pompot.server.parser.PomDirectoryScanner;
import com.pompot.server.parser.PomDirectoryScanner.ScanResult;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Command executed in CLI mode to print a report of repeated values across pom.xml files.
 */
public final class CommonValueReportCommand {

    private final PomDirectoryScanner pomDirectoryScanner;
    private final CommonValueExtractor commonValueExtractor;

    public CommonValueReportCommand(
        PomDirectoryScanner pomDirectoryScanner,
        CommonValueExtractor commonValueExtractor
    ) {
        this.pomDirectoryScanner = Objects.requireNonNull(pomDirectoryScanner, "pomDirectoryScanner");
        this.commonValueExtractor = Objects.requireNonNull(commonValueExtractor, "commonValueExtractor");
    }

    /**
     * Executes the report for the provided directory.
     *
     * @param directory directory containing pom.xml files.
     * @param out stream used to print the report.
     * @param err stream used to report errors.
     * @return zero when the report runs successfully; non-zero when the directory is invalid.
     */
    public int run(String directory, PrintStream out, PrintStream err) {
        if (directory == null || directory.isBlank()) {
            err.println("--report-common-values requires a directory argument.");
            return 1;
        }

        Path root;
        try {
            root = Path.of(expandLeadingTilde(directory)).toAbsolutePath().normalize();
        } catch (InvalidPathException exception) {
            err.printf("Invalid directory provided: %s%n", directory);
            return 1;
        }

        if (!Files.isDirectory(root)) {
            err.printf("Directory not found: %s%n", root);
            return 1;
        }

        ScanResult scan = pomDirectoryScanner.scan(root);
        if (!scan.foundPomFiles()) {
            out.printf("No pom.xml files were found under %s%n", root);
            return 0;
        }

        List<CommonValue> repeatedValues = commonValueExtractor.extract(scan.parsedPoms());
        if (repeatedValues.isEmpty()) {
            out.printf("No repeated values were detected under %s%n", root);
            return 0;
        }

        printReport(scan, repeatedValues, out);
        return 0;
    }

    private void printReport(ScanResult scan, List<CommonValue> values, PrintStream out) {
        int categoryWidth = Math.max(
            "Category".length(),
            values.stream().map(CommonValue::category).mapToInt(String::length).max().orElse(0)
        );
        int identifierWidth = Math.max(
            "Identifier".length(),
            values.stream().map(CommonValue::identifier).mapToInt(String::length).max().orElse(0)
        );
        int valueWidth = Math.max(
            "Value".length(),
            values.stream().map(CommonValue::value).mapToInt(String::length).max().orElse(0)
        );
        int occurrencesWidth = Math.max(
            "Occurrences".length(),
            values.stream().mapToInt(CommonValue::occurrences).map(value -> Integer.toString(value).length()).max().orElse(0)
        );

        String format = String.format(
            "%%-%ds  %%-%ds  %%-%ds  %%%ds%%n",
            categoryWidth,
            identifierWidth,
            valueWidth,
            occurrencesWidth
        );

        out.printf("Repeated values under %s%n", scan.root());
        out.println();
        out.printf(format, "Category", "Identifier", "Value", "Occurrences");
        out.printf(
            format,
            "-".repeat(categoryWidth),
            "-".repeat(identifierWidth),
            "-".repeat(valueWidth),
            "-".repeat(occurrencesWidth)
        );

        values
            .stream()
            .sorted(Comparator
                .comparing(CommonValue::category, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(CommonValue::identifier, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(CommonValue::value, String.CASE_INSENSITIVE_ORDER))
            .forEach(value -> out.printf(
                format,
                value.category(),
                value.identifier(),
                value.value(),
                Integer.toString(value.occurrences())
            ));
    }

    private String expandLeadingTilde(String candidate) {
        String trimmed = candidate == null ? null : candidate.trim();
        if (trimmed == null || !trimmed.startsWith("~")) {
            return trimmed == null ? "" : trimmed;
        }

        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            return trimmed;
        }

        if (trimmed.length() == 1) {
            return userHome;
        }

        char next = trimmed.charAt(1);
        if (next == '/' || next == '\\') {
            return userHome + trimmed.substring(1);
        }

        return trimmed;
    }
}
