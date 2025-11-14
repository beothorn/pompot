package com.pompot.server.pomgraph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Payload stored in a {@link GraphEdge}. Values can either wrap a textual
 * reference or expose a tree of nested {@link GraphValue} instances for complex
 * structures such as dependencies.
 */
public sealed interface GraphValue permits GraphValue.Textual, GraphValue.Composite {

    /**
     * Creates a value backed by a {@link TextReference}. The reference is shared
     * as-is so updates remain visible to every edge that stores it.
     *
     * @param reference reference to expose through the value.
     * @return immutable graph value wrapping the provided reference.
     */
    static GraphValue text(TextReference reference) {
        return new Textual(reference);
    }

    /**
     * Creates a hierarchical value formed by child entries. The provided map is
     * copied to guarantee immutability.
     *
     * @param entries mapping between entry name and the nested value.
     * @return immutable graph value containing the provided structure.
     */
    static GraphValue composite(Map<String, GraphValue> entries) {
        return new Composite(entries);
    }

    /**
     * Accessor for the textual reference stored by the value.
     *
     * @return optional containing the wrapped {@link TextReference} when the
     *     value is textual.
     */
    Optional<TextReference> text();

    /**
     * Child entries attached to the value.
     *
     * @return immutable view of nested values. Textual values expose an empty
     *     map.
     */
    Map<String, GraphValue> children();

    /**
     * Implementation used for textual payloads.
     */
    final class Textual implements GraphValue {

        private final TextReference reference;

        Textual(TextReference reference) {
            this.reference = Objects.requireNonNull(reference, "reference");
        }

        @Override
        public Optional<TextReference> text() {
            return Optional.of(reference);
        }

        @Override
        public Map<String, GraphValue> children() {
            return Map.of();
        }
    }

    /**
     * Implementation used for hierarchical payloads.
     */
    final class Composite implements GraphValue {

        private final Map<String, GraphValue> entries;

        Composite(Map<String, GraphValue> entries) {
            Map<String, GraphValue> copy = new LinkedHashMap<>();
            for (Map.Entry<String, GraphValue> entry : entries.entrySet()) {
                String key = Objects.requireNonNull(entry.getKey(), "entryKey").trim();
                if (key.isEmpty()) {
                    continue;
                }
                GraphValue value = Objects.requireNonNull(entry.getValue(), "entryValue");
                copy.put(key, value);
            }
            this.entries = Collections.unmodifiableMap(copy);
        }

        @Override
        public Optional<TextReference> text() {
            return Optional.empty();
        }

        @Override
        public Map<String, GraphValue> children() {
            return entries;
        }
    }
}
