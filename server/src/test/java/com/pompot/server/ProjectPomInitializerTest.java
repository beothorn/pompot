package com.pompot.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pompot.server.parser.ParsedPomCollection;
import com.pompot.server.parser.ParsedPomRepository;
import com.pompot.server.parser.PomFileParser;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.maven.model.io.DefaultModelReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class ProjectPomInitializerTest {

    private ParsedPomRepository parsedPomRepository;
    private ProjectPomInitializer initializer;
    private Path temporaryHomeCopy;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        PomFileParser parser = new PomFileParser(new DefaultModelReader(), objectMapper);
        parsedPomRepository = new ParsedPomRepository();
        initializer = new ProjectPomInitializer(parser, parsedPomRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (temporaryHomeCopy != null) {
            deleteRecursively(temporaryHomeCopy);
            temporaryHomeCopy = null;
        }
    }

    @Test
    void storesParsedPomsWhenParentArgumentIsPresent() throws Exception {
        String projectsRoot = Path.of("src", "test", "resources", "projects").toString();
        DefaultApplicationArguments arguments = new DefaultApplicationArguments(new String[]{"--parent=" + projectsRoot});

        initializer.run(arguments);

        Optional<ParsedPomCollection> storedPom = parsedPomRepository.fetch();
        assertTrue(storedPom.isPresent(), "Expected parsed poms to be stored");
        assertEquals(2, storedPom.get().entries().size(), "Expected both sample projects to be parsed");
    }

    @Test
    void clearsRepositoryWhenDirectoryHasNoPoms() throws Exception {
        Path emptyDirectory = Files.createTempDirectory("pompot-empty");
        parsedPomRepository.store(new ParsedPomCollection("placeholder", java.util.List.of()));
        DefaultApplicationArguments arguments = new DefaultApplicationArguments(new String[]{"--parent=" + emptyDirectory});

        initializer.run(arguments);

        Optional<ParsedPomCollection> storedPom = parsedPomRepository.fetch();
        assertTrue(storedPom.isEmpty(), "Expected repository to be cleared when no pom files are present");
    }

    @Test
    void expandsTildeInParentArgument() throws Exception {
        Path projectsRoot = Path.of("src", "test", "resources", "projects").toAbsolutePath();
        temporaryHomeCopy = copyProjectsToHome(projectsRoot);

        Path home = Path.of(System.getProperty("user.home")).toAbsolutePath().normalize();
        Path normalizedCopy = temporaryHomeCopy.toAbsolutePath().normalize();
        String relativePath = home.relativize(normalizedCopy).toString().replace('\\', '/');
        String tildeArgument = relativePath.isEmpty() ? "~" : "~/" + relativePath;

        DefaultApplicationArguments arguments = new DefaultApplicationArguments(new String[]{"--parent=" + tildeArgument});

        initializer.run(arguments);

        Optional<ParsedPomCollection> storedPom = parsedPomRepository.fetch();
        assertTrue(storedPom.isPresent(), "Expected parsed poms when using a tilde path");
        assertEquals(2, storedPom.get().entries().size(), "Expected both sample projects to be parsed from the tilde path");
    }

    private Path copyProjectsToHome(Path sourceRoot) throws IOException {
        Path home = Path.of(System.getProperty("user.home")).toAbsolutePath().normalize();
        Path targetRoot = Files.createTempDirectory(home, "pompot-projects-");

        Files.walkFileTree(sourceRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = sourceRoot.relativize(dir);
                Path destination = targetRoot.resolve(relative.toString());
                Files.createDirectories(destination);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = sourceRoot.relativize(file);
                Path destination = targetRoot.resolve(relative.toString());
                Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });

        return targetRoot;
    }

    private void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }

        try (Stream<Path> walker = Files.walk(root)) {
            walker
                .sorted((left, right) -> right.compareTo(left))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                        // Best-effort cleanup for temporary test files.
                    }
                });
        }
    }
}
