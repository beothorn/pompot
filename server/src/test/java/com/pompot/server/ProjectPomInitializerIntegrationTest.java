package com.pompot.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pompot.server.parser.ParsedPomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifies that the application parses pom files when the --parent argument is provided.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        args = "--parent=src/test/resources/projects")
class ProjectPomInitializerIntegrationTest {

    @Autowired
    private ParsedPomRepository repository;

    @Test
    void storesParsedPomDuringStartup() {
        assertTrue(repository.fetch().isPresent(), "Expected parsed poms to be stored on startup");
    }
}
