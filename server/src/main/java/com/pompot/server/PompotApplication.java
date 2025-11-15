package com.pompot.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pompot.server.arguments.ApplicationMode;
import com.pompot.server.cli.CommonValueReportCommand;
import com.pompot.server.parser.CommonValueExtractor;
import com.pompot.server.parser.PomDirectoryScanner;
import com.pompot.server.parser.PomFileParser;
import java.util.Map;
import java.util.Optional;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.ModelReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for both the CLI and UI modes of the application, handling argument parsing and bootstrapping Spring.
 * UI mode configures the embedded server to listen on {@link #DEFAULT_PORT} so the HTTP controller becomes reachable.
 */
@SpringBootApplication
public class PompotApplication {

    public static final String DEFAULT_PORT = "9754";

    /**
     * Launches the application respecting the selected mode.
     * CLI mode prints the product banner and exits, while UI mode starts Spring Boot on {@link #DEFAULT_PORT}.
     * @param args command-line arguments passed to the JVM.
     */
    public static void main(String[] args) {
        Optional<String> reportArgument = extractCommonValueReportArgument(args);
        if (reportArgument.isPresent()) {
            runCommonValueReport(reportArgument.get());
            return;
        }

        ApplicationMode mode = ApplicationMode.fromArguments(args);
        // We check if the mode is headless
        if (mode.isCli()) {
            printAbout();
            return;
        }

        SpringApplication application = new SpringApplication(PompotApplication.class);
        application.setDefaultProperties(Map.of("server.port", DEFAULT_PORT));
        application.run(args);
    }

    /**
     * Prints the CLI banner with the resolved version so headless users receive the same product information.
     */
    private static void printAbout() {
        String detectedVersion = Optional.ofNullable(PompotApplication.class.getPackage().getImplementationVersion())
            .orElse("development");
        System.out.printf("pompot %s - workspace manager prototype%n", detectedVersion);
    }

    private static Optional<String> extractCommonValueReportArgument(String[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return Optional.empty();
        }

        for (int index = 0; index < arguments.length; index += 1) {
            String argument = arguments[index];
            if (argument == null) {
                continue;
            }

            if (argument.startsWith("--report-common-values=")) {
                return Optional.of(argument.substring("--report-common-values=".length()));
            }

            if ("--report-common-values".equals(argument)) {
                if (index + 1 < arguments.length) {
                    String value = arguments[index + 1];
                    if (value != null && !value.startsWith("--")) {
                        return Optional.of(value);
                    }
                }
                return Optional.of("");
            }
        }

        return Optional.empty();
    }

    private static void runCommonValueReport(String directory) {
        ModelReader modelReader = new DefaultModelReader();
        ObjectMapper objectMapper = new ObjectMapper();
        PomFileParser parser = new PomFileParser(modelReader, objectMapper);
        PomDirectoryScanner scanner = new PomDirectoryScanner(parser);
        CommonValueExtractor extractor = new CommonValueExtractor();
        CommonValueReportCommand command = new CommonValueReportCommand(scanner, extractor);
        command.run(directory, System.out, System.err);
    }
}
