package com.pompot.server.arguments;

import java.util.Arrays;

/**
 * Represents the application mode chosen on start.
 * It can be cli (headless) or ui (browser mode)
 */
public enum ApplicationMode {
    UI,
    CLI;

    /**
     * Parse the arguments to get the mode.
     * @param arguments The arguments from the command line.
     * @return The application mode, ui or cli.
     */
    public static ApplicationMode fromArguments(String[] arguments) {
        boolean cliRequested = Arrays.stream(arguments)
            .anyMatch(argument -> "--mode=cli".equalsIgnoreCase(argument));
        return cliRequested ? CLI : UI;
    }

    /**
     * Checks if the application mode passed on the execution on cli is cli or ui.
     * @return true if is cli, false is ui (browser mode)
     */
    public boolean isCli() {
        return this == CLI;
    }
}
