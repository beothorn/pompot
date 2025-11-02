package com.pompot.server;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Repository;

@Repository
class ParsedPomRepository {

    private final AtomicReference<ParsedPom> storage = new AtomicReference<>();

    Optional<ParsedPom> fetch() {
        return Optional.ofNullable(storage.get());
    }

    void store(ParsedPom parsedPom) {
        storage.set(parsedPom);
    }

    void clear() {
        storage.set(null);
    }
}
