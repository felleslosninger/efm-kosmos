package no.difi.move.deploymanager.service.codesigner;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandBuilderTest {

    @Test
    public void testSimpleBuild() {
        assertThat(new CommandBuilder("jarPath")
                .build()
        ).containsExactly("jarsigner", "-strict", "-verify", "jarPath");
    }

    @Test
    public void testKeystore() {
        assertThat(new CommandBuilder("jarPath")
                .keystore("/tmp/keystore.jks")
                .build()
        ).containsExactly("jarsigner", "-strict", "-keystore", "/tmp/keystore.jks", "-verify", "jarPath");
    }

    @Test
    public void testPassword() {
        assertThat(new CommandBuilder("jarPath")
                .password("xxx")
                .build()
        ).containsExactly("jarsigner", "-strict", "-storepass", "xxx", "-verify", "jarPath");
    }

    @Test
    public void testAlias() {
        assertThat(new CommandBuilder("jarPath")
                .alias("stuntman")
                .build()
        ).containsExactly("jarsigner", "-strict", "-verify", "jarPath", "stuntman");
    }
}
