package com.pompot.server.arguments;

import java.util.Arrays;

/**
 * Represents the application mode chosen on start.
 * UI mode boots the HTTP server, while CLI mode keeps the process headless.
 */
public enum ApplicationMode {
    UI,
    CLI;

    /**
     * Parses the command-line arguments to determine the requested mode.
     * @param arguments arguments received by {@link com.pompot.server.PompotApplication#main(String[])}.
     * @return {@link #CLI} when "--mode=cli" is present; {@link #UI} otherwise.
     */
    public static ApplicationMode fromArguments(String[] arguments) {
        boolean cliRequested = Arrays.stream(arguments)
            .anyMatch(argument -> "--mode=cli".equalsIgnoreCase(argument));
        return cliRequested ? CLI : UI;
    }

    /**
     * Indicates whether the application should run without the HTTP server.
     * @return {@code true} for CLI mode; {@code false} for UI mode.
     */
    public boolean isCli() {
        return this == CLI;
    }
}
