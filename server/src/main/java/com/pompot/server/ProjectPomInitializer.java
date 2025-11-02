package com.pompot.server;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.pompot.server.parser.ParsedPom;
import com.pompot.server.parser.ParsedPomRepository;
import com.pompot.server.parser.PomFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class ProjectPomInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectPomInitializer.class);

    private final PomFileParser pomFileParser;
    private final ParsedPomRepository parsedPomRepository;

    ProjectPomInitializer(PomFileParser pomFileParser, ParsedPomRepository parsedPomRepository) {
        this.pomFileParser = pomFileParser;
        this.parsedPomRepository = parsedPomRepository;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (!arguments.containsOption("project")) {
            LOGGER.info("No --project argument supplied; pom parsing skipped.");
            parsedPomRepository.clear();
            return;
        }

        List<String> values = arguments.getOptionValues("project");
        if (values == null || values.isEmpty()) {
            LOGGER.warn("--project argument present without a value; pom parsing skipped.");
            parsedPomRepository.clear();
            return;
        }

        String projectRootValue = values.get(0);
        Path projectRoot;
        try {
            projectRoot = Path.of(projectRootValue);
        } catch (InvalidPathException exception) {
            LOGGER.error("Invalid project path provided: {}", projectRootValue, exception);
            parsedPomRepository.clear();
            return;
        }

        Optional<JsonNode> parsedModel = pomFileParser.parse(projectRoot);
        if (parsedModel.isEmpty()) {
            parsedPomRepository.clear();
            return;
        }

        ParsedPom parsedPom = new ParsedPom(projectRoot.toAbsolutePath().toString(), parsedModel.get());
        parsedPomRepository.store(parsedPom);
        LOGGER.info("Parsed pom.xml from {}", parsedPom.projectRoot());
    }
}
