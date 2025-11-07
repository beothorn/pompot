package com.pompot.server.parser;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Snapshot of a parsed pom.xml including metadata useful for the UI.
 *
 * @param pomPath absolute path to the pom file.
 * @param relativePath relative path from the scanned root to the pom file.
 * @param groupId Maven group identifier resolved for the pom.
 * @param artifactId Maven artifact identifier resolved for the pom.
 * @param model JSON representation of the Maven model produced by {@link PomFileParser}.
 */
public record ParsedPom(String pomPath, String relativePath, String groupId, String artifactId, JsonNode model) {
}
