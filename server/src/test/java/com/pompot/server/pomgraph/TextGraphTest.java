package com.pompot.server.pomgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TextGraphTest {

    @Test
    void addNodeReturnsSameInstance() {
        TextGraph graph = new TextGraph();

        GraphNode first = graph.addNode("pom:one");
        GraphNode second = graph.addNode("pom:one");

        assertSame(first, second, "Graph should reuse the same node instance");
        assertEquals(1, graph.nodes().size(), "Only one node should be stored");
    }

    @Test
    void connectStoresEdgeWithTextPayload() {
        TextGraph graph = new TextGraph();
        GraphNode pom = graph.addNode("pom:project");
        GraphNode property = graph.addNode("property:version");
        TextReference version = graph.createText("1.0.0");

        GraphEdge edge = pom.connect("property", property, version);

        assertSame(version, edge.value().text().orElseThrow(), "Edge should store the shared text reference");
        Collection<GraphEdge> edges = pom.edges("property");
        assertEquals(1, edges.size(), "Pom node should expose the property edge");
        GraphEdge stored = edges.iterator().next();
        assertSame(property, stored.target(), "Stored edge should point to the property node");
        assertEquals(
            "1.0.0",
            stored.value().text().orElseThrow().value().value(),
            "Edge payload should expose the text value"
        );
    }

    @Test
    void sharedTextReferenceReflectsUpdatesAcrossEdges() {
        TextGraph graph = new TextGraph();
        GraphNode pomOne = graph.addNode("pom:one");
        GraphNode pomTwo = graph.addNode("pom:two");
        GraphNode dependency = graph.addNode("dependency:commons-lang3");
        TextReference version = graph.createText("3.12.0");

        pomOne.connect("dependency", dependency, version);
        pomTwo.connect("dependency", dependency, version);

        version.update("3.13.0");

        assertTrue(pomOne
            .edges("dependency")
            .stream()
            .allMatch(edge -> edge.value().text().orElseThrow().value().value().equals("3.13.0")));
        assertTrue(pomTwo
            .edges("dependency")
            .stream()
            .allMatch(edge -> edge.value().text().orElseThrow().value().value().equals("3.13.0")));
    }

    @Test
    void copyCreatesIndependentStructureSharingTextReferences() {
        TextGraph graph = new TextGraph();
        GraphNode pom = graph.addNode("pom:project");
        GraphNode property = graph.addNode("property:version");
        TextReference version = graph.createText("1.0.0");
        pom.connect("property", property, version);

        TextGraph copy = graph.copy();

        assertNotSame(graph, copy, "Copy should produce a different instance");
        GraphNode copiedPom = copy.findNode("pom:project").orElseThrow();
        GraphNode copiedProperty = copy.findNode("property:version").orElseThrow();

        assertNotSame(pom, copiedPom, "Node instances should not be shared across copies");
        assertNotSame(property, copiedProperty, "Target nodes should be re-created on copy");

        GraphEdge copiedEdge = copiedPom.edges("property").iterator().next();
        assertSame(version, copiedEdge.value().text().orElseThrow(), "Text references must be shared across copies");

        copiedEdge.value().text().orElseThrow().update("2.0.0");

        GraphEdge originalEdge = pom.edges("property").iterator().next();
        assertEquals(
            "2.0.0",
            originalEdge.value().text().orElseThrow().value().value(),
            "Updating the shared text reference should reflect on the original graph"
        );
    }

    @Test
    void connectAcceptsCompositePayloads() {
        TextGraph graph = new TextGraph();
        GraphNode pom = graph.addNode("pom:project");
        GraphNode dependency = graph.addNode("dependency:com.example:demo");

        TextReference version = graph.createText("1.0.0");
        TextReference scope = graph.createText("compile");
        GraphValue payload = GraphValue.composite(Map.of(
            "version",
            GraphValue.text(version),
            "scope",
            GraphValue.text(scope)
        ));

        GraphEdge edge = pom.connect("dependency", dependency, payload);

        Map<String, GraphValue> children = edge.value().children();
        assertEquals(2, children.size(), "Composite payload should expose child entries");
        assertSame(version, children.get("version").text().orElseThrow(), "Version reference should be stored");
        assertSame(scope, children.get("scope").text().orElseThrow(), "Scope reference should be stored");
    }
}
