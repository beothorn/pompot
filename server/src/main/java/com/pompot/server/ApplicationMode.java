package com.pompot.server;

import java.util.Arrays;

enum ApplicationMode {
    UI,
    CLI;

    static ApplicationMode fromArguments(String[] arguments) {
        boolean cliRequested = Arrays.stream(arguments)
            .anyMatch(argument -> "--mode=cli".equalsIgnoreCase(argument));
        return cliRequested ? CLI : UI;
    }

    boolean isCli() {
        return this == CLI;
    }
}
