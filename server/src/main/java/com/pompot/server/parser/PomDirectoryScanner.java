package com.pompot.server.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traverses a directory looking for pom.xml files and converts them into {@link ParsedPom}
 * entries using the provided {@link PomFileParser}.
 */
public class PomDirectoryScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomDirectoryScanner.class);

    private final PomFileParser pomFileParser;

    public PomDirectoryScanner(PomFileParser pomFileParser) {
        this.pomFileParser = Objects.requireNonNull(pomFileParser, "pomFileParser");
    }

    /**
     * Scans the provided root directory looking for pom.xml files.
     *
     * @param root directory that contains the pom files to parse.
     * @return scan outcome describing the parsed pom entries.
     */
    public ScanResult scan(Path root) {
        if (root == null) {
            return new ScanResult(null, false, List.of());
        }

        Path normalizedRoot = root.toAbsolutePath().normalize();
        if (!Files.isDirectory(normalizedRoot)) {
            LOGGER.error("Provided path is not a directory: {}", normalizedRoot);
            return new ScanResult(normalizedRoot, false, List.of());
        }

        List<Path> pomFiles;
        try (Stream<Path> walker = Files.walk(normalizedRoot)) {
            pomFiles = walker
                .filter(Files::isRegularFile)
                .filter(this::isPomXmlFile)
                .collect(Collectors.toList());
        } catch (IOException exception) {
            LOGGER.error("Failed to traverse {}", normalizedRoot, exception);
            return new ScanResult(normalizedRoot, false, List.of());
        }

        boolean foundPomFiles = !pomFiles.isEmpty();
        if (!foundPomFiles) {
            return new ScanResult(normalizedRoot, false, List.of());
        }

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
            return new ScanResult(normalizedRoot, true, List.of());
        }

        parsedPoms.sort(Comparator
            .comparing(ParsedPom::groupId, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .thenComparing(ParsedPom::artifactId, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .thenComparing(ParsedPom::relativePath));

        return new ScanResult(normalizedRoot, true, List.copyOf(parsedPoms));
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

    private String deriveRelativePath(Path root, Path pomFile) {
        if (root == null || pomFile == null) {
            return pomFile == null ? "" : pomFile.toString();
        }

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

    /**
     * Result produced by {@link #scan(Path)} describing the parsed poms.
     *
     * @param root directory that was scanned.
     * @param foundPomFiles whether at least one pom.xml file was discovered.
     * @param parsedPoms immutable list with the parsed entries.
     */
    public record ScanResult(Path root, boolean foundPomFiles, List<ParsedPom> parsedPoms) {

        public ScanResult {
            root = root == null ? null : root.toAbsolutePath().normalize();
            parsedPoms = List.copyOf(parsedPoms);
        }
    }
}
