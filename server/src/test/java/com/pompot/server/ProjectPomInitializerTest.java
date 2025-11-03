package com.pompot.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.maven.model.io.DefaultModelReader;
import com.pompot.server.parser.ParsedPom;
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
    void storesParsedPomWhenProjectArgumentIsPresent() throws Exception {
        String projectPath = Path.of("src", "test", "resources", "projects", "simple").toString();
        DefaultApplicationArguments arguments = new DefaultApplicationArguments(new String[]{"--project=" + projectPath});

        initializer.run(arguments);

        Optional<ParsedPom> storedPom = parsedPomRepository.fetch();
        assertTrue(storedPom.isPresent(), "Expected parsed pom to be stored");
    }

    @Test
    void clearsRepositoryWhenArgumentMissing() throws Exception {
        parsedPomRepository.store(new ParsedPom("placeholder", new ObjectMapper().createObjectNode()));
        DefaultApplicationArguments arguments = new DefaultApplicationArguments(new String[0]);

        initializer.run(arguments);

        Optional<ParsedPom> storedPom = parsedPomRepository.fetch();
        assertTrue(storedPom.isEmpty(), "Expected repository to be cleared when no project is provided");
    }
}
