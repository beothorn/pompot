package com.pompot.server.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pompot.server.pomgraph.GraphNode;
import com.pompot.server.pomgraph.GraphValue;
import com.pompot.server.pomgraph.TextGraph;
import com.pompot.server.pomgraph.TextReference;
import java.util.LinkedHashMap;
import java.util.List;
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

    private ParsedPom createParsedPom(String pomPath, java.util.function.Consumer<TextGraph> graphPopulator) {
        TextGraph graph = new TextGraph();
        graphPopulator.accept(graph);
        JsonNode emptyModel = objectMapper.createObjectNode();
        return new ParsedPom(pomPath, pomPath, null, null, emptyModel, graph);
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
