package com.pompot.server.parser;

import java.util.List;

/**
 * Collection of parsed pom entries produced during initialization.
 *
 * @param scannedRoot absolute path of the directory that was scanned for pom files.
 * @param entries parsed pom entries discovered under the root.
 */
public record ParsedPomCollection(String scannedRoot, List<ParsedPom> entries) {

    public ParsedPomCollection {
        entries = List.copyOf(entries);
    }
}
