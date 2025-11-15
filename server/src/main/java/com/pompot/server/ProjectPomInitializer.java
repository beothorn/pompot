package com.pompot.server;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import com.pompot.server.parser.ParsedPom;
import com.pompot.server.parser.CommonValue;
import com.pompot.server.parser.CommonValueExtractor;
import com.pompot.server.parser.ParsedPomCollection;
import com.pompot.server.parser.PomDirectoryScanner;
import com.pompot.server.parser.ParsedPomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Bootstraps pom parsing when the application starts in UI mode.
 */
@Component
class ProjectPomInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectPomInitializer.class);

    private final ParsedPomRepository parsedPomRepository;
    private final CommonValueExtractor commonValueExtractor;
    private final PomDirectoryScanner pomDirectoryScanner;

    /**
     * Creates the initializer with parsing collaborators.
     * @param parsedPomRepository repository that stores the parsed result.
     * @param commonValueExtractor component that aggregates repeated values.
     * @param pomDirectoryScanner scanner used to discover and parse pom files.
     */
    ProjectPomInitializer(
        ParsedPomRepository parsedPomRepository,
        CommonValueExtractor commonValueExtractor,
        PomDirectoryScanner pomDirectoryScanner
    ) {
        this.parsedPomRepository = parsedPomRepository;
        this.commonValueExtractor = commonValueExtractor;
        this.pomDirectoryScanner = pomDirectoryScanner;
    }

    /**
     * Scans the working directory (or the --parent argument) and parses every pom.xml found.
     * @param arguments command-line arguments supplied to the Spring application.
     */
    @Override
    public void run(ApplicationArguments arguments) {
        Path scanRoot = resolveScanRoot(arguments);
        if (scanRoot == null) {
            parsedPomRepository.clear();
            return;
        }

        PomDirectoryScanner.ScanResult scanResult = pomDirectoryScanner.scan(scanRoot);
        if (!scanResult.foundPomFiles()) {
            LOGGER.info("No pom.xml files found under {}", scanRoot.toAbsolutePath().normalize());
            parsedPomRepository.clear();
            return;
        }

        List<ParsedPom> parsedPoms = scanResult.parsedPoms();
        if (parsedPoms.isEmpty()) {
            LOGGER.warn("Failed to parse pom.xml files under {}", scanResult.root());
            parsedPomRepository.clear();
            return;
        }

        List<CommonValue> commonValues = commonValueExtractor.extract(parsedPoms);
        ParsedPomCollection collection = new ParsedPomCollection(
            scanResult.root().toString(),
            parsedPoms,
            commonValues
        );
        parsedPomRepository.store(collection);
        LOGGER.info("Parsed {} pom.xml files under {}", parsedPoms.size(), scanResult.root());
    }

    private Path resolveScanRoot(ApplicationArguments arguments) {
        if (arguments.containsOption("parent")) {
            List<String> values = arguments.getOptionValues("parent");
            if (values == null || values.isEmpty()) {
                LOGGER.warn("--parent argument present without a value; pom parsing skipped.");
                return null;
            }

            String candidate = values.get(0);
            if (candidate == null || candidate.isBlank()) {
                LOGGER.warn("--parent argument present without a value; pom parsing skipped.");
                return null;
            }

            try {
                Path provided = Path.of(expandLeadingTilde(candidate)).toAbsolutePath().normalize();
                if (!Files.isDirectory(provided)) {
                    LOGGER.error("Provided parent path is not a directory: {}", provided);
                    return null;
                }
                return provided;
            } catch (InvalidPathException exception) {
                LOGGER.error("Invalid parent path provided: {}", candidate, exception);
                return null;
            }
        }

        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        if (!Files.isDirectory(workingDirectory)) {
            LOGGER.error("Working directory is not a directory: {}", workingDirectory);
            return null;
        }

        return workingDirectory;
    }

    private String expandLeadingTilde(String candidate) {
        if (candidate == null) {
            return null;
        }

        String trimmed = candidate.trim();
        if (!trimmed.startsWith("~")) {
            return trimmed;
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
