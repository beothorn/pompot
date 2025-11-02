package com.pompot.server.parser;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Repository;

@Repository
public class ParsedPomRepository {

    private final AtomicReference<ParsedPom> storage = new AtomicReference<>();

    public Optional<ParsedPom> fetch() {
        return Optional.ofNullable(storage.get());
    }

    public void store(ParsedPom parsedPom) {
        storage.set(parsedPom);
    }

    public void clear() {
        storage.set(null);
    }
}
