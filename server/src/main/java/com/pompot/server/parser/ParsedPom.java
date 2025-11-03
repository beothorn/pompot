package com.pompot.server.parser;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Snapshot of the parsed pom.xml, containing the project root and its JSON representation.
 */
public record ParsedPom(String projectRoot, JsonNode model) {
}
