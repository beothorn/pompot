package com.pompot.server.pomgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
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

        assertSame(version, edge.value(), "Edge should store the shared text reference");
        Collection<GraphEdge> edges = pom.edges("property");
        assertEquals(1, edges.size(), "Pom node should expose the property edge");
        GraphEdge stored = edges.iterator().next();
        assertSame(property, stored.target(), "Stored edge should point to the property node");
        assertEquals("1.0.0", stored.value().value().value(), "Edge payload should expose the text value");
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

        assertTrue(pomOne.edges("dependency").stream().allMatch(edge -> edge.value().value().value().equals("3.13.0")));
        assertTrue(pomTwo.edges("dependency").stream().allMatch(edge -> edge.value().value().value().equals("3.13.0")));
    }
}
