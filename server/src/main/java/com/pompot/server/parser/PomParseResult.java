package com.pompot.server.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.pompot.server.pomgraph.TextGraph;

/**
 * Result of parsing a pom.xml file containing metadata and its JSON representation.
 *
 * @param groupId Maven group identifier resolved for the pom.
 * @param artifactId Maven artifact identifier resolved for the pom.
 * @param model JSON representation of the Maven model.
 * @param graph graph representation populated from the Maven model.
 */
public record PomParseResult(String groupId, String artifactId, JsonNode model, TextGraph graph) {
}
