package com.pompot.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.pompot.server.parser.ParsedPom;
import com.pompot.server.parser.CommonValue;
import com.pompot.server.parser.CommonValueExtractor;
import com.pompot.server.parser.ParsedPomCollection;
import com.pompot.server.parser.ParsedPomRepository;
import com.pompot.server.parser.PomFileParser;
import com.pompot.server.parser.PomParseResult;
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

    private final PomFileParser pomFileParser;
    private final ParsedPomRepository parsedPomRepository;
    private final CommonValueExtractor commonValueExtractor;

    /**
     * Creates the initializer with parsing collaborators.
     * @param pomFileParser parser used to read pom.xml files.
     * @param parsedPomRepository repository that stores the parsed result.
     */
    ProjectPomInitializer(
        PomFileParser pomFileParser,
        ParsedPomRepository parsedPomRepository,
        CommonValueExtractor commonValueExtractor
    ) {
        this.pomFileParser = pomFileParser;
        this.parsedPomRepository = parsedPomRepository;
        this.commonValueExtractor = commonValueExtractor;
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

        List<Path> pomFiles;
        try (Stream<Path> walker = Files.walk(scanRoot)) {
            pomFiles = walker
                .filter(Files::isRegularFile)
                .filter(this::isPomXmlFile)
                .collect(Collectors.toList());
        } catch (IOException exception) {
            LOGGER.error("Failed to traverse {}", scanRoot.toAbsolutePath(), exception);
            parsedPomRepository.clear();
            return;
        }

        if (pomFiles.isEmpty()) {
            LOGGER.info("No pom.xml files found under {}", scanRoot.toAbsolutePath());
            parsedPomRepository.clear();
            return;
        }

        Path normalizedRoot = scanRoot.toAbsolutePath().normalize();
        List<ParsedPom> parsedPoms = new ArrayList<>();

        for (Path pomFile : pomFiles) {
            Path projectRoot = pomFile.getParent();
            if (projectRoot == null) {
                continue;
            }

            Optional<PomParseResult> parseResult = pomFileParser.parse(projectRoot);
            if (parseResult.isEmpty()) {
                continue;
            }

            Path absolutePom = pomFile.toAbsolutePath().normalize();
            String relativePath = deriveRelativePath(normalizedRoot, absolutePom);
            PomParseResult result = parseResult.get();
            ParsedPom parsedPom = new ParsedPom(
                absolutePom.toString(),
                relativePath,
                emptyToNull(result.groupId()),
                emptyToNull(result.artifactId()),
                result.model(),
                result.graph()
            );
            parsedPoms.add(parsedPom);
        }

        if (parsedPoms.isEmpty()) {
            LOGGER.warn("Failed to parse pom.xml files under {}", normalizedRoot);
            parsedPomRepository.clear();
            return;
        }

        parsedPoms.sort(Comparator
            .comparing(ParsedPom::groupId, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .thenComparing(ParsedPom::artifactId, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .thenComparing(ParsedPom::relativePath));

        List<CommonValue> commonValues = commonValueExtractor.extract(parsedPoms);
        ParsedPomCollection collection = new ParsedPomCollection(
            normalizedRoot.toString(),
            List.copyOf(parsedPoms),
            commonValues
        );
        parsedPomRepository.store(collection);
        LOGGER.info("Parsed {} pom.xml files under {}", parsedPoms.size(), normalizedRoot);
    }

    private boolean isPomXmlFile(Path candidate) {
        if (candidate == null) {
            return false;
        }

        Path fileName = candidate.getFileName();
        if (fileName == null) {
            return false;
        }

        return "pom.xml".equalsIgnoreCase(fileName.toString());
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

    private String deriveRelativePath(Path root, Path pomFile) {
        try {
            return root.relativize(pomFile).toString();
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("Could not relativize {} against {}", pomFile, root, exception);
            return pomFile.toString();
        }
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }
}
