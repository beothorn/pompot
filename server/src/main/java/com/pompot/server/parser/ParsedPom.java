package com.pompot.server.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.pompot.server.pomgraph.TextGraph;
import java.util.Objects;

/**
 * Snapshot of a parsed pom.xml including metadata useful for the UI.
 *
 * @param pomPath absolute path to the pom file.
 * @param relativePath relative path from the scanned root to the pom file.
 * @param groupId Maven group identifier resolved for the pom.
 * @param artifactId Maven artifact identifier resolved for the pom.
 * @param model JSON representation of the Maven model produced by {@link PomFileParser}.
 * @param graph graph representation mirroring the pom contents.
 */
public record ParsedPom(
    String pomPath,
    String relativePath,
    String groupId,
    String artifactId,
    JsonNode model,
    TextGraph graph) {

    public ParsedPom {
        graph = Objects.requireNonNull(graph, "graph").copy();
    }

    @Override
    public TextGraph graph() {
        return graph.copy();
    }
}
