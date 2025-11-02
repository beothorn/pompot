package com.pompot.server.parser;

import com.fasterxml.jackson.databind.JsonNode;

public record ParsedPom(String projectRoot, JsonNode model) {
}
