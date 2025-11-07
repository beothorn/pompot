package com.pompot.server.parser;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Snapshot of the parsed pom.xml, containing the project root and its JSON representation.
 *
 * @param projectRoot absolute path to the project whose pom.xml was parsed.
 * @param model JSON representation of the Maven model produced by {@link PomFileParser}.
 */
public record ParsedPom(String projectRoot, JsonNode model) {
}
