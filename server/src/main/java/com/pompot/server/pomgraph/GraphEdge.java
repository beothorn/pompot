package com.pompot.server.pomgraph;

import java.util.Objects;

/**
 * Connection between two nodes carrying a {@link GraphValue} as payload.
 */
public record GraphEdge(GraphNode source, GraphNode target, String relationship, GraphValue value) {

    public GraphEdge {
        source = Objects.requireNonNull(source, "source");
        target = Objects.requireNonNull(target, "target");
        relationship = Objects.requireNonNull(relationship, "relationship").trim();
        value = Objects.requireNonNull(value, "value");
    }
}
