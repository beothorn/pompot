package com.pompot.server.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pompot.server.pomgraph.GraphEdge;
import com.pompot.server.pomgraph.GraphNode;
import com.pompot.server.pomgraph.TextGraph;
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

        Optional<PomParseResult> parsedModel = parser.parse(projectRoot);

        assertTrue(parsedModel.isPresent(), "Expected pom.xml to be parsed");
        PomParseResult result = parsedModel.orElseThrow();
        assertEquals("com.example", result.groupId());
        assertEquals("demo", result.artifactId());
        assertEquals("com.example", result.model().path("groupId").asText());
        assertEquals("demo", result.model().path("artifactId").asText());

        TextGraph graph = result.graph();
        assertNotNull(graph, "Graph should be present in the parse result");
        GraphNode pomNode = graph
            .findNode("pom:" + projectRoot.toAbsolutePath().normalize())
            .orElseThrow(() -> new AssertionError("Pom node not found"));
        GraphEdge groupEdge = pomNode.edges("groupId").iterator().next();
        assertEquals("com.example", groupEdge.value().value().value());
        GraphEdge artifactEdge = pomNode.edges("artifactId").iterator().next();
        assertEquals("demo", artifactEdge.value().value().value());
    }

    @Test
    void parsesPomWithPluginConfigurationWithoutRecursing() {
        Path projectRoot = Path.of("src", "test", "resources", "projects", "with-plugin-config");
        PomFileParser parser = new PomFileParser(new DefaultModelReader(), objectMapper);

        Optional<PomParseResult> parsedModel = parser.parse(projectRoot);

        assertTrue(parsedModel.isPresent(), "Expected pom with plugin configuration to be parsed");
        PomParseResult result = parsedModel.orElseThrow();
        assertEquals("com.example", result.groupId());
        assertEquals("plugin-config-project", result.artifactId());
        var modelNode = result.model();

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

        TextGraph graph = result.graph();
        assertNotNull(graph, "Graph should be generated for pom with plugin configuration");
        GraphNode pomNode = graph
            .findNode("pom:" + projectRoot.toAbsolutePath().normalize())
            .orElseThrow(() -> new AssertionError("Pom node not found"));
        assertTrue(pomNode.edges("dependency").isEmpty(), "Sample project should not have direct dependencies");
    }
}
