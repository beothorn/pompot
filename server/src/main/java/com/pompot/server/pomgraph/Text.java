package com.pompot.server.pomgraph;

import java.util.Objects;

/**
 * Immutable textual value stored inside the pom graph.
 */
public record Text(String value) {

    public Text {
        value = Objects.requireNonNullElse(value, "");
    }
}
