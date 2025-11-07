package com.pompot.server.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.maven.model.io.DefaultModelReader;
import org.junit.jupiter.api.Test;

class PomFileParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesPomIntoJsonTree() {
        Path projectRoot = Path.of("src", "test", "resources", "projects", "simple");
        PomFileParser parser = new PomFileParser(new DefaultModelReader(), objectMapper);

        Optional<JsonNode> parsedModel = parser.parse(projectRoot);

        assertTrue(parsedModel.isPresent(), "Expected pom.xml to be parsed");
        JsonNode modelNode = parsedModel.orElseThrow();
        assertEquals("com.example", modelNode.path("groupId").asText());
        assertEquals("demo", modelNode.path("artifactId").asText());
    }

    @Test
    void parsesPomWithPluginConfigurationWithoutRecursing() {
        Path projectRoot = Path.of("src", "test", "resources", "projects", "with-plugin-config");
        PomFileParser parser = new PomFileParser(new DefaultModelReader(), objectMapper);

        Optional<JsonNode> parsedModel = parser.parse(projectRoot);

        assertTrue(parsedModel.isPresent(), "Expected pom with plugin configuration to be parsed");
        JsonNode modelNode = parsedModel.orElseThrow();

        JsonNode pluginsNode = modelNode.path("build").path("plugins");
        assertTrue(pluginsNode.isArray(), () -> "Unexpected plugins node: " + pluginsNode.toPrettyString());

        JsonNode firstPlugin = pluginsNode.get(0);
        assertTrue(firstPlugin != null && !firstPlugin.isMissingNode(), () -> "Plugins array empty: " + pluginsNode);

        JsonNode pluginConfiguration = firstPlugin.path("configuration");
        assertTrue(!pluginConfiguration.isMissingNode(), () -> "Plugin JSON: " + firstPlugin.toPrettyString());
        assertEquals(2, pluginConfiguration.path("childCount").asInt(), pluginConfiguration::toPrettyString);
        JsonNode sourceChild = pluginConfiguration.path("children").get(0);
        assertEquals("source", sourceChild.path("name").asText(), pluginConfiguration::toPrettyString);
        assertEquals("17", sourceChild.path("value").asText(), pluginConfiguration::toPrettyString);

        JsonNode executionsNode = firstPlugin.path("executions");
        assertTrue(executionsNode.isArray(), () -> "Executions node: " + executionsNode.toPrettyString());

        JsonNode executionConfiguration = executionsNode.get(0).path("configuration");
        assertTrue(!executionConfiguration.isMissingNode(), () -> "Execution JSON: " + executionsNode.toPrettyString());
        JsonNode executionChild = executionConfiguration.path("children").get(0);
        assertEquals("showWarnings", executionChild.path("name").asText(), executionConfiguration::toPrettyString);
        assertEquals("true", executionChild.path("value").asText(), executionConfiguration::toPrettyString);

        JsonNode reportingConfiguration = modelNode
            .path("reporting")
            .path("plugins")
            .get(0)
            .path("configuration");
        assertTrue(!reportingConfiguration.isMissingNode(), () -> "Reporting JSON: " + reportingConfiguration.toPrettyString());
        JsonNode reportingChild = reportingConfiguration.path("children").get(0);
        assertEquals("linkOnly", reportingChild.path("name").asText(), reportingConfiguration::toPrettyString);
        assertEquals("false", reportingChild.path("value").asText(), reportingConfiguration::toPrettyString);
    }
}
