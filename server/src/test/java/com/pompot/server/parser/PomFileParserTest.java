package com.pompot.server.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class PomFileParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesPomIntoJsonTree() {
        Path projectRoot = Path.of("src", "test", "resources", "projects", "simple");
        PomFileParser parser = new PomFileParser(objectMapper);

        Optional<JsonNode> parsedModel = parser.parse(projectRoot);

        assertTrue(parsedModel.isPresent(), "Expected pom.xml to be parsed");
        JsonNode modelNode = parsedModel.orElseThrow();
        assertEquals("com.example", modelNode.path("groupId").asText());
        assertEquals("demo", modelNode.path("artifactId").asText());
    }
}
