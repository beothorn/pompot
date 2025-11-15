package com.pompot.server.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pompot.server.parser.CommonValueExtractor;
import com.pompot.server.parser.PomDirectoryScanner;
import com.pompot.server.parser.PomFileParser;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.apache.maven.model.io.DefaultModelReader;
import org.junit.jupiter.api.Test;

class CommonValueReportCommandTest {

    @Test
    void printsReportForRepeatedValues() {
        ObjectMapper objectMapper = new ObjectMapper();
        PomFileParser parser = new PomFileParser(new DefaultModelReader(), objectMapper);
        PomDirectoryScanner scanner = new PomDirectoryScanner(parser);
        CommonValueReportCommand command = new CommonValueReportCommand(scanner, new CommonValueExtractor());

        Path projectsRoot = Path.of("src", "test", "resources", "cli-report");
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();

        int exitCode = command.run(
            projectsRoot.toString(),
            new PrintStream(standardOutput),
            new PrintStream(errorOutput)
        );

        assertEquals(0, exitCode, "Report should succeed for valid directories");

        String report = standardOutput.toString(StandardCharsets.UTF_8);
        assertTrue(report.contains("Repeated values under"), "Report header should be present");
        assertTrue(report.contains("bom"), "BOM entries should be listed");
        assertTrue(report.contains("com.example:platform-bom:pom [import]"), "BOM identifier should be included");
        assertTrue(report.contains("plugin"), "Plugin entries should be listed");
        assertTrue(report.contains("managed plugin"), "Managed plugin entries should be listed");
        assertTrue(report.contains("tile"), "Tile entries should be listed");
        assertTrue(errorOutput.toString(StandardCharsets.UTF_8).isEmpty(), "No errors expected for valid runs");
    }

    @Test
    void reportsInvalidDirectory() {
        ObjectMapper objectMapper = new ObjectMapper();
        PomFileParser parser = new PomFileParser(new DefaultModelReader(), objectMapper);
        PomDirectoryScanner scanner = new PomDirectoryScanner(parser);
        CommonValueReportCommand command = new CommonValueReportCommand(scanner, new CommonValueExtractor());

        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();

        int exitCode = command.run(
            "does-not-exist",
            new PrintStream(standardOutput),
            new PrintStream(errorOutput)
        );

        assertEquals(1, exitCode, "Invalid directories should produce a non-zero exit code");
        String errors = errorOutput.toString(StandardCharsets.UTF_8);
        assertTrue(errors.contains("Directory not found"), "Expected an error message for missing directories");
    }
}
