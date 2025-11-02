package com.pompot.server;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pom")
class ProjectPomController {

    private final ParsedPomRepository parsedPomRepository;

    ProjectPomController(ParsedPomRepository parsedPomRepository) {
        this.parsedPomRepository = parsedPomRepository;
    }

    @GetMapping
    ResponseEntity<ParsedPom> fetchParsedPom() {
        Optional<ParsedPom> parsedPom = parsedPomRepository.fetch();
        if (parsedPom.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(parsedPom.get());
    }
}
