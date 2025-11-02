package com.pompot.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.pompot.server.arguments.ApplicationMode;
import org.junit.jupiter.api.Test;

class ApplicationModeTest {

    @Test
    void detectsCliModeWhenFlagIsPresent() {
        ApplicationMode mode = ApplicationMode.fromArguments(new String[] {"--mode=cli"});

        assertThat(mode).isEqualTo(ApplicationMode.CLI);
    }

    @Test
    void defaultsToUiMode() {
        ApplicationMode mode = ApplicationMode.fromArguments(new String[] {});

        assertThat(mode).isEqualTo(ApplicationMode.UI);
    }
}
