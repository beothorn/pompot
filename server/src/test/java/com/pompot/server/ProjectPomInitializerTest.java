package com.pompot.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.maven.model.io.DefaultModelReader;
import com.pompot.server.parser.ParsedPomCollection;
import com.pompot.server.parser.ParsedPomRepository;
import com.pompot.server.parser.PomFileParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class ProjectPomInitializerTest {

    private ParsedPomRepository parsedPomRepository;
    private ProjectPomInitializer initializer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        PomFileParser parser = new PomFileParser(new DefaultModelReader(), objectMapper);
        parsedPomRepository = new ParsedPomRepository();
        initializer = new ProjectPomInitializer(parser, parsedPomRepository);
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
}
