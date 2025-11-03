package com.pompot.server.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.ModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Parses pom.xml files from a project root into JSON representations so the UI can render them.
 */
@Component
public class PomFileParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomFileParser.class);

    private final ModelReader modelReader;
    private final ObjectMapper objectMapper;

    @Autowired
    public PomFileParser(ObjectMapper objectMapper) {
        this(new DefaultModelReader(), objectMapper);
    }

    /**
     * Creates a parser using the provided Maven model reader and JSON mapper.
     * @param modelReader reader capable of interpreting pom.xml files.
     * @param objectMapper mapper used to convert the parsed model into JSON.
     */
    public PomFileParser(ModelReader modelReader, ObjectMapper objectMapper) {
        this.modelReader = modelReader;
        this.objectMapper = objectMapper;
    }

    /**
     * Parses the pom.xml located inside the provided project root.
     * @param projectRoot path to the project that contains the pom.xml file.
     * @return the parsed pom as JSON when successful, otherwise {@link Optional#empty()}.
     */
    public Optional<JsonNode> parse(Path projectRoot) {
        if (projectRoot == null) {
            return Optional.empty();
        }

        Path pomLocation = projectRoot.resolve("pom.xml");
        if (!Files.isRegularFile(pomLocation)) {
            LOGGER.warn("pom.xml not found at {}", pomLocation.toAbsolutePath());
            return Optional.empty();
        }

        try {
            File pomFile = pomLocation.toFile();
            Map<String, ?> options = Map.of(ModelReader.IS_STRICT, Boolean.FALSE);
            Model model = modelReader.read(pomFile, options);
            JsonNode asJson = objectMapper.valueToTree(model);
            return Optional.ofNullable(asJson);
        } catch (IOException exception) {
            LOGGER.error("Failed to parse pom.xml at {}", pomLocation.toAbsolutePath(), exception);
            return Optional.empty();
        }
    }
}
