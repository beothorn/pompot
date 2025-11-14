package com.pompot.server.pomgraph;

import java.util.Objects;

/**
 * Mutable handle that allows sharing {@link Text} instances across graph edges.
 */
public final class TextReference {

    private final String id;
    private Text value;

    TextReference(String id, Text value) {
        this.id = Objects.requireNonNull(id, "id");
        this.value = Objects.requireNonNull(value, "value");
    }

    /**
     * Identifier assigned by the owning graph. It stays stable even if the
     * underlying text changes.
     * @return unique identifier for this reference.
     */
    public String id() {
        return id;
    }

    /**
     * Current text stored in the reference.
     * @return immutable text instance.
     */
    public Text value() {
        return value;
    }

    /**
     * Replaces the stored text with a new value.
     * @param newValue text that should be visible to every consumer holding
     *     this reference.
     */
    public void update(Text newValue) {
        value = Objects.requireNonNull(newValue, "newValue");
    }

    /**
     * Convenience overload that accepts a raw string and wraps it in a
     * {@link Text} instance.
     * @param newValue raw text to store.
     */
    public void update(String newValue) {
        update(new Text(newValue));
    }
}
