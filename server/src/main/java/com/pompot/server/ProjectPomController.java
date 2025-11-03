package com.pompot.server;

import java.util.Optional;

import com.pompot.server.parser.ParsedPom;
import com.pompot.server.parser.ParsedPomRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes parsed pom metadata to the UI via HTTP endpoints.
 */
@RestController
@RequestMapping("/api/pom")
class ProjectPomController {

    private final ParsedPomRepository parsedPomRepository;

    ProjectPomController(ParsedPomRepository parsedPomRepository) {
        this.parsedPomRepository = parsedPomRepository;
    }

    /**
     * Returns the parsed pom model stored in memory, if any.
     * @return HTTP 200 with the parsed model or 404 when nothing is stored.
     */
    @GetMapping
    ResponseEntity<ParsedPom> fetchParsedPom() {
        Optional<ParsedPom> parsedPom = parsedPomRepository.fetch();
        if (parsedPom.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(parsedPom.get());
    }
}
