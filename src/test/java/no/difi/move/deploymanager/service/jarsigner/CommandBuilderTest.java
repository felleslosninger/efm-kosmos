package no.difi.move.deploymanager.service.jarsigner;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandBuilderTest {

    @Test
    public void testSimpleBuild() {
        assertThat(new CommandBuilder("jarPath")
                .build()
        ).containsExactly("jarsigner", "-verify", "jarPath");
    }

    @Test
    public void testKeystore() {
        assertThat(new CommandBuilder("jarPath")
                .keystore("/tmp/keystore.jks")
                .build()
        ).containsExactly("jarsigner", "-verify", "-keystore", "/tmp/keystore.jks", "jarPath");
    }

    @Test
    public void testPassword() {
        assertThat(new CommandBuilder("jarPath")
                .password("xxx")
                .build()
        ).containsExactly("jarsigner", "-verify", "-storepass", "xxx", "jarPath");
    }

    @Test
    public void testAlias() {
        assertThat(new CommandBuilder("jarPath")
                .alias("stuntman")
                .build()
        ).containsExactly("jarsigner", "-verify", "jarPath", "stuntman");
    }
}
