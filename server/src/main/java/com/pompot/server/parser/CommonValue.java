package com.pompot.server.parser;

import java.util.Objects;

/**
 * Describes a value repeated across parsed pom files. It captures the category
 * of the value, a descriptive identifier and the number of occurrences.
 *
 * @param category logical group for the value (for example {@code property} or
 *     {@code dependency}).
 * @param identifier human readable name describing what the value refers to.
 * @param value actual repeated value.
 * @param occurrences number of times the value appeared across the parsed poms.
 */
public record CommonValue(String category, String identifier, String value, int occurrences) {

    public CommonValue {
        category = Objects.requireNonNull(category, "category").trim();
        identifier = Objects.requireNonNull(identifier, "identifier").trim();
        value = Objects.requireNonNull(value, "value").trim();
        if (category.isEmpty()) {
            throw new IllegalArgumentException("category");
        }
        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("identifier");
        }
        if (value.isEmpty()) {
            throw new IllegalArgumentException("value");
        }
        if (occurrences < 2) {
            throw new IllegalArgumentException("occurrences must be greater than 1");
        }
    }
}
