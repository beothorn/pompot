package com.pompot.server;

import java.util.Map;
import java.util.Optional;

import com.pompot.server.arguments.ApplicationMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point for our application.
 * This will be both the entrypoint the will call parsing of arguments and star the spring application.
 */
@SpringBootApplication
public class PompotApplication {

    public static final String DEFAULT_PORT = "9754";

    /**
     * The entrypoint for the application.
     * @param args
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
     * In case of cli, for now we just print "about".
     */
    private static void printAbout() {
        String detectedVersion = Optional.ofNullable(PompotApplication.class.getPackage().getImplementationVersion())
            .orElse("development");
        System.out.printf("pompot %s - workspace manager prototype%n", detectedVersion);
    }
}
