package com.pompot.server;

import java.util.Map;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PompotApplication {

    public static void main(String[] args) {
        ApplicationMode mode = ApplicationMode.fromArguments(args);
        if (mode.isCli()) {
            printAbout();
            return;
        }

        SpringApplication application = new SpringApplication(PompotApplication.class);
        application.setDefaultProperties(Map.of("server.port", "9754"));
        application.run(args);
    }

    private static void printAbout() {
        String detectedVersion = Optional.ofNullable(PompotApplication.class.getPackage().getImplementationVersion())
            .orElse("development");
        System.out.printf("pompot %s - workspace manager prototype%n", detectedVersion);
    }
}
