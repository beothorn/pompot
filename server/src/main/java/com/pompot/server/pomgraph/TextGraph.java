package com.pompot.server.pomgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Directed graph where edges carry {@link TextReference} payloads.
 */
public final class TextGraph {

    private final Map<String, GraphNode> nodes;
    private final Map<String, TextReference> texts;

    public TextGraph() {
        this.nodes = new LinkedHashMap<>();
        this.texts = new LinkedHashMap<>();
    }

    /**
     * Copy constructor used to create a structural clone of an existing graph.
     * The cloned graph reuses the {@link TextReference} instances so updates to
     * the shared textual values remain visible across copies.
     *
     * @param source graph to copy.
     */
    public TextGraph(TextGraph source) {
        this();
        TextGraph other = Objects.requireNonNull(source, "source");

        Map<String, GraphNode> clonedNodes = new LinkedHashMap<>();
        for (GraphNode node : other.nodes.values()) {
            GraphNode clone = new GraphNode(node.id());
            nodes.put(clone.id(), clone);
            clonedNodes.put(clone.id(), clone);
        }

        for (Map.Entry<String, TextReference> entry : other.texts.entrySet()) {
            texts.put(entry.getKey(), entry.getValue());
        }

        for (GraphNode node : other.nodes.values()) {
            GraphNode clonedSource = clonedNodes.get(node.id());
            for (GraphEdge edge : node.edges()) {
                GraphNode clonedTarget = clonedNodes.get(edge.target().id());
                clonedSource.connect(edge.relationship(), clonedTarget, edge.value());
            }
        }
    }

    /**
     * Creates or returns the node with the provided identifier.
     * @param id identifier of the node to look up.
     * @return existing or newly created node.
     */
    public GraphNode addNode(String id) {
        return nodes.computeIfAbsent(Objects.requireNonNull(id, "id"), GraphNode::new);
    }

    /**
     * Retrieves a node by identifier.
     * @param id identifier to look up.
     * @return optional containing the node when present.
     */
    public Optional<GraphNode> findNode(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(nodes.get(id));
    }

    /**
     * Immutable view of the nodes stored in the graph.
     * @return nodes sorted by insertion order.
     */
    public Collection<GraphNode> nodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /**
     * Creates a new {@link TextReference} containing the provided value.
     * @param value raw string to wrap.
     * @return reference managed by the graph.
     */
    public TextReference createText(String value) {
        String identifier = UUID.randomUUID().toString();
        TextReference reference = new TextReference(identifier, new Text(value));
        texts.put(identifier, reference);
        return reference;
    }

    /**
     * All {@link TextReference} instances managed by the graph.
     * @return immutable snapshot of registered text references.
     */
    public Collection<TextReference> texts() {
        return Collections.unmodifiableCollection(texts.values());
    }

    /**
     * Creates a structural clone of the graph sharing the {@link TextReference}
     * instances with the original graph. It allows exposing the graph without
     * leaking the internal node maps.
     *
     * @return cloned graph with identical structure.
     */
    public TextGraph copy() {
        return new TextGraph(this);
    }
}
