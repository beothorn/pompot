package com.pompot.server.parser;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Repository;

/**
 * Stores the parsed pom result in-memory so controllers can serve it.
 */
@Repository
public class ParsedPomRepository {

    private final AtomicReference<ParsedPom> storage = new AtomicReference<>();

    /**
     * Retrieves the stored parsed pom.
     * @return the current parsed pom when present.
     */
    public Optional<ParsedPom> fetch() {
        return Optional.ofNullable(storage.get());
    }

    /**
     * Replaces the stored parsed pom with a new value.
     * @param parsedPom parsed pom information to store.
     */
    public void store(ParsedPom parsedPom) {
        storage.set(parsedPom);
    }

    /**
     * Removes any stored parsed pom.
     */
    public void clear() {
        storage.set(null);
    }
}
