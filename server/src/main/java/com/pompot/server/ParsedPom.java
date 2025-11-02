package com.pompot.server;

import com.fasterxml.jackson.databind.JsonNode;

record ParsedPom(String projectRoot, JsonNode model) {
}
