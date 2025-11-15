package com.pompot.server.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pompot.server.pomgraph.GraphEdge;
import com.pompot.server.pomgraph.GraphNode;
import com.pompot.server.pomgraph.GraphValue;
import com.pompot.server.pomgraph.TextGraph;
import com.pompot.server.pomgraph.TextReference;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import org.apache.maven.model.io.DefaultModelReader;
import org.junit.jupiter.api.Test;

class CommonValueExtractorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void extractReturnsRepeatedPropertiesAndDependencies() {
        CommonValueExtractor extractor = new CommonValueExtractor();

        ParsedPom first = createParsedPom(
            "/projects/one/pom.xml",
            graph -> {
                GraphNode pom = graph.addNode("pom:/projects/one");
                GraphNode property = graph.addNode("property:java.version");
                TextReference javaVersion = graph.createText("17");
                pom.connect("property", property, javaVersion);

                GraphNode dependency = graph.addNode("dependency:com.example:demo");
                GraphValue dependencyValue = dependencyValue(graph, "com.example", "demo", "1.2.3", null);
                pom.connect("dependency", dependency, dependencyValue);

                GraphNode managed = graph.addNode("dependency:com.example:demo-managed");
                GraphValue managedValue = dependencyValue(graph, "com.example", "demo", "1.2.3", null);
                pom.connect("managedDependency", managed, managedValue);
            }
        );

        ParsedPom second = createParsedPom(
            "/projects/two/pom.xml",
            graph -> {
                GraphNode pom = graph.addNode("pom:/projects/two");
                GraphNode property = graph.addNode("property:java.version");
                TextReference javaVersion = graph.createText("17");
                pom.connect("property", property, javaVersion);

                GraphNode dependency = graph.addNode("dependency:com.example:demo");
                GraphValue dependencyValue = dependencyValue(graph, "com.example", "demo", "1.2.3", null);
                pom.connect("dependency", dependency, dependencyValue);

                GraphNode managed = graph.addNode("dependency:com.example:demo-managed");
                GraphValue managedValue = dependencyValue(graph, "com.example", "demo", "1.2.3", null);
                pom.connect("managedDependency", managed, managedValue);

                GraphNode uniqueProperty = graph.addNode("property:encoding");
                pom.connect("property", uniqueProperty, graph.createText("UTF-8"));
            }
        );

        List<CommonValue> values = extractor.extract(List.of(first, second));

        assertEquals(3, values.size(), "Repeated dependencies and properties should be reported");

        CommonValue dependencyValue = values.get(0);
        assertEquals("dependency", dependencyValue.category());
        assertEquals("com.example:demo", dependencyValue.identifier());
        assertEquals("1.2.3", dependencyValue.value());
        assertEquals(2, dependencyValue.occurrences());

        CommonValue managedValue = values.get(1);
        assertEquals("managed dependency", managedValue.category());
        assertEquals("com.example:demo", managedValue.identifier());
        assertEquals("1.2.3", managedValue.value());
        assertEquals(2, managedValue.occurrences());

        CommonValue propertyValue = values.get(2);
        assertEquals("property", propertyValue.category());
        assertEquals("java.version", propertyValue.identifier());
        assertEquals("17", propertyValue.value());
        assertEquals(2, propertyValue.occurrences());
    }

    @Test
    void parsesPomResourcesAndDetectsRepeatedValues() {
        Path projectsRoot = Path.of("src", "test", "resources", "projects-with-common-values");
        Path alphaRoot = projectsRoot.resolve("alpha");
        Path betaRoot = projectsRoot.resolve("beta");

        PomFileParser parser = new PomFileParser(new DefaultModelReader(), objectMapper);

        PomParseResult alphaResult = parser
            .parse(alphaRoot)
            .orElseThrow(() -> new AssertionError("Expected alpha pom to be parsed"));
        PomParseResult betaResult = parser
            .parse(betaRoot)
            .orElseThrow(() -> new AssertionError("Expected beta pom to be parsed"));

        TextGraph alphaGraph = alphaResult.graph();
        TextGraph betaGraph = betaResult.graph();

        ParsedPom alphaPom = parsedPom(alphaRoot, alphaResult, alphaGraph, "alpha/pom.xml");
        ParsedPom betaPom = parsedPom(betaRoot, betaResult, betaGraph, "beta/pom.xml");

        CommonValueExtractor extractor = new CommonValueExtractor();
        List<CommonValue> values = extractor.extract(List.of(alphaPom, betaPom));

        assertEquals(3, values.size(), "Repeated dependency, managed dependency and property should be reported");

        CommonValue dependencyValue = values.get(0);
        assertEquals("dependency", dependencyValue.category());
        assertEquals("com.example:shared-library:jar", dependencyValue.identifier());
        assertEquals("1.2.3", dependencyValue.value());
        assertEquals(2, dependencyValue.occurrences());

        CommonValue managedValue = values.get(1);
        assertEquals("managed dependency", managedValue.category());
        assertEquals("com.example:managed-shared:jar", managedValue.identifier());
        assertEquals("9.9.9", managedValue.value());
        assertEquals(2, managedValue.occurrences());

        CommonValue propertyValue = values.get(2);
        assertEquals("property", propertyValue.category());
        assertEquals("java.version", propertyValue.identifier());
        assertEquals("17", propertyValue.value());
        assertEquals(2, propertyValue.occurrences());

        assertJavaVersionProperty(alphaRoot, alphaGraph, "17");
        assertJavaVersionProperty(betaRoot, betaGraph, "17");

        assertDependencyEdge(alphaRoot, alphaGraph, "dependency:com.example:shared-library:jar", "dependency", "1.2.3");
        assertDependencyEdge(betaRoot, betaGraph, "dependency:com.example:shared-library:jar", "dependency", "1.2.3");
        assertDependencyEdge(alphaRoot, alphaGraph, "dependency:com.example:managed-shared:jar", "managedDependency", "9.9.9");
        assertDependencyEdge(betaRoot, betaGraph, "dependency:com.example:managed-shared:jar", "managedDependency", "9.9.9");
    }

    private ParsedPom createParsedPom(String pomPath, java.util.function.Consumer<TextGraph> graphPopulator) {
        TextGraph graph = new TextGraph();
        graphPopulator.accept(graph);
        JsonNode emptyModel = objectMapper.createObjectNode();
        return new ParsedPom(pomPath, pomPath, null, null, emptyModel, graph);
    }

    private ParsedPom parsedPom(Path projectRoot, PomParseResult result, TextGraph graph, String relativePath) {
        return new ParsedPom(
            projectRoot.resolve("pom.xml").toAbsolutePath().normalize().toString(),
            relativePath,
            result.groupId(),
            result.artifactId(),
            result.model(),
            graph
        );
    }

    private void assertJavaVersionProperty(Path projectRoot, TextGraph graph, String expectedValue) {
        GraphNode pomNode = findPomNode(projectRoot, graph);
        GraphEdge propertyEdge = pomNode
            .edges("property")
            .stream()
            .filter(edge -> "property:java.version".equals(edge.target().id()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("property edge not found"));

        String actual = propertyEdge.value().text().orElseThrow().value().value();
        assertEquals(expectedValue, actual);
    }

    private void assertDependencyEdge(
        Path projectRoot,
        TextGraph graph,
        String expectedTargetId,
        String relationship,
        String expectedVersion
    ) {
        GraphNode pomNode = findPomNode(projectRoot, graph);
        GraphEdge dependencyEdge = pomNode
            .edges(relationship)
            .stream()
            .filter(edge -> expectedTargetId.equals(edge.target().id()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("dependency edge not found"));

        GraphValue version = dependencyEdge.value().children().get("version");
        assertNotNull(version, "dependency version should be recorded");
        String actual = version.text().orElseThrow().value().value();
        assertEquals(expectedVersion, actual);
    }

    private GraphNode findPomNode(Path projectRoot, TextGraph graph) {
        String nodeId = "pom:" + projectRoot.toAbsolutePath().normalize();
        Optional<GraphNode> node = graph.findNode(nodeId);
        assertTrue(node.isPresent(), () -> "Pom node not found: " + nodeId);
        return node.orElseThrow();
    }

    private GraphValue dependencyValue(
        TextGraph graph,
        String groupId,
        String artifactId,
        String version,
        String scope
    ) {
        LinkedHashMap<String, GraphValue> payload = new LinkedHashMap<>();
        payload.put("version", GraphValue.text(graph.createText(version)));
        if (groupId != null) {
            payload.put("groupId", GraphValue.text(graph.createText(groupId)));
        }
        if (artifactId != null) {
            payload.put("artifactId", GraphValue.text(graph.createText(artifactId)));
        }
        if (scope != null) {
            payload.put("scope", GraphValue.text(graph.createText(scope)));
        }
        return GraphValue.composite(payload);
    }
}
