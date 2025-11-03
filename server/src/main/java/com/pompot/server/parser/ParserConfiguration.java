package com.pompot.server.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.ModelReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides parser-related Spring beans.
 */
@Configuration
public class ParserConfiguration {

    /**
     * Supplies the Maven model reader used to parse pom.xml files.
     * @return a {@link DefaultModelReader} instance.
     */
    @Bean
    ModelReader modelReader() {
        return new DefaultModelReader();
    }

    /**
     * Creates the {@link PomFileParser} using Spring managed collaborators.
     * @param modelReader reader capable of parsing pom.xml files.
     * @param objectMapper mapper that converts the parsed model into JSON.
     * @return a configured {@link PomFileParser}.
     */
    @Bean
    PomFileParser pomFileParser(ModelReader modelReader, ObjectMapper objectMapper) {
        return new PomFileParser(modelReader, objectMapper);
    }
}
