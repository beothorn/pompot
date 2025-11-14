package com.pompot.server.pomgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Node stored in the {@link TextGraph}. It keeps track of outgoing edges grouped
 * by relationship name.
 */
public final class GraphNode {

    private final String id;
    private final Map<String, List<GraphEdge>> edges;

    GraphNode(String id) {
        this.id = Objects.requireNonNull(id, "id").trim();
        this.edges = new LinkedHashMap<>();
    }

    /**
     * Identifier assigned during creation.
     * @return immutable identifier of the node.
     */
    public String id() {
        return id;
    }

    /**
     * Creates an outgoing edge toward the provided target node.
     * @param relationship name of the relationship represented by the edge.
     * @param target node that receives the connection.
     * @param value payload shared by the edge.
     * @return created edge instance.
     */
    public GraphEdge connect(String relationship, GraphNode target, TextReference value) {
        GraphEdge edge = new GraphEdge(this, target, relationship, value);
        edges.computeIfAbsent(edge.relationship(), key -> new ArrayList<>()).add(edge);
        return edge;
    }

    /**
     * Retrieves every outgoing edge stored in this node.
     * @return immutable snapshot of outgoing edges.
     */
    public Collection<GraphEdge> edges() {
        List<GraphEdge> collected = new ArrayList<>();
        for (List<GraphEdge> group : edges.values()) {
            collected.addAll(group);
        }
        return Collections.unmodifiableList(collected);
    }

    /**
     * Retrieves outgoing edges for the provided relationship.
     * @param relationship identifier of the relationship to inspect.
     * @return immutable view of the edges associated with the relationship.
     */
    public Collection<GraphEdge> edges(String relationship) {
        List<GraphEdge> group = edges.get(Objects.requireNonNull(relationship, "relationship"));
        if (group == null) {
            return List.of();
        }
        return Collections.unmodifiableList(group);
    }
}
