package com.pompot.server;

import java.util.Map;
import java.util.Optional;

import com.pompot.server.arguments.ApplicationMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for both the CLI and UI modes of the application, handling argument parsing and bootstrapping Spring.
 * UI mode configures the embedded server to listen on {@link #DEFAULT_PORT} so the HTTP controller becomes reachable.
 */
@SpringBootApplication
public class PompotApplication {

    public static final String DEFAULT_PORT = "9754";

    /**
     * Launches the application respecting the selected mode.
     * CLI mode prints the product banner and exits, while UI mode starts Spring Boot on {@link #DEFAULT_PORT}.
     * @param args command-line arguments passed to the JVM.
     */
    public static void main(String[] args) {
        ApplicationMode mode = ApplicationMode.fromArguments(args);
        // We check if the mode is headless
        if (mode.isCli()) {
            printAbout();
            return;
        }

        SpringApplication application = new SpringApplication(PompotApplication.class);
        application.setDefaultProperties(Map.of("server.port", DEFAULT_PORT));
        application.run(args);
    }

    /**
     * Prints the CLI banner with the resolved version so headless users receive the same product information.
     */
    private static void printAbout() {
        String detectedVersion = Optional.ofNullable(PompotApplication.class.getPackage().getImplementationVersion())
            .orElse("development");
        System.out.printf("pompot %s - workspace manager prototype%n", detectedVersion);
    }
}
