package no.difi.move.kosmos.repo;

import no.difi.move.kosmos.config.BlocklistProperties;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.util.KosmosUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KosmosDirectoryJavaArchiveRepositoryTest {

    @TempDir
    Path tempDir;

    private KosmosDirectoryJavaArchiveRepository repo;
    private KosmosProperties properties;
    private Clock clock;

    @BeforeEach
    void setUp() {
        properties = mock(KosmosProperties.class);
        IntegrasjonspunktProperties integrasjonspunktProperties = mock(IntegrasjonspunktProperties.class);
        when(properties.getIntegrasjonspunkt()).thenReturn(integrasjonspunktProperties);
        when(integrasjonspunktProperties.getHome()).thenReturn(tempDir.toString());

        BlocklistProperties blocklistProperties = mock(BlocklistProperties.class);
        when(properties.getBlocklist()).thenReturn(blocklistProperties);
        when(blocklistProperties.getDurationInHours()).thenReturn(1);

        // all tests start at 10:00
        clock = Clock.fixed(Instant.parse("2026-02-17T10:00:00Z"), ZoneId.of("UTC"));
        repo = new KosmosDirectoryJavaArchiveRepository(properties, clock);
    }

    @Test
    void isBlockListed_notExists_returnsFalse() {
        File jarFile = new File(tempDir.toFile(), "test.jar");
        assertFalse(repo.isBlockListed(jarFile));
    }

    @Test
    void isBlockListed_notExpired_returnsTrue() throws IOException {
        File jarFile = new File(tempDir.toFile(), "test.jar");
        File blocklistFile = new File(tempDir.toFile(), "test.blocklisted");
        // Expires in 1 hour (11:00)
        Files.writeString(blocklistFile.toPath(), LocalDateTime.now(clock).plusHours(1).toString());

        assertTrue(repo.isBlockListed(jarFile));
        assertTrue(blocklistFile.exists());
    }

    @Test
    void isBlockListed_expired_returnsFalseAndRemovesFile() throws IOException {
        File jarFile = new File(tempDir.toFile(), "test.jar");
        File blocklistFile = new File(tempDir.toFile(), "test.blocklisted");
        // Expired 1 hour ago (09:00)
        Files.writeString(blocklistFile.toPath(), LocalDateTime.now(clock).minusHours(1).toString());

        assertFalse(repo.isBlockListed(jarFile));
        assertFalse(blocklistFile.exists());
    }

    @Test
    void doBlocklist_createsFileWithCorrectExpiration() {
        File jarFile = new File(tempDir.toFile(), "test.jar");
        File blocklistFile = repo.getBlocklistPath(jarFile);

        repo.blockList(jarFile);

        assertTrue(blocklistFile.exists());
        // Should expire at 11:00 (1 hour after 10:00)
        String expectedContent = LocalDateTime.now(clock).plusHours(1).toString();
        try {
            assertEquals(expectedContent, Files.readString(blocklistFile.toPath()));
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void getSemanticVersion() {
        assertNull(repo.getSemanticVersion("filename-1"));
        assertNull(repo.getSemanticVersion("filename-1.2"));
        assertEquals("1.2.3", repo.getSemanticVersion("filename-1.2.3"));
        assertEquals("2.28.5", repo.getSemanticVersion("filename-2.28.5"));
        assertEquals("3.0.0", repo.getSemanticVersion("filename-3.0.0"));
        assertEquals("4.0.0", repo.getSemanticVersion("filename-4.0.0"));
        assertEquals("4.0.0-beta", repo.getSemanticVersion("filename-4.0.0-beta"));
        // from IPv4 we use tagging and they have not been consistent, so we need to ignore leading "v" og "V"
        assertEquals("4.0.1", repo.getSemanticVersion("filename-v4.0.1"));
        assertEquals("4.1.0", repo.getSemanticVersion("filename-V4.1.0"));
        assertEquals("4.2.1-beta", repo.getSemanticVersion("filename-v4.2.1-beta"));
        assertEquals("4.3.0-rc", repo.getSemanticVersion("filename-V4.3.0-rc"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2.31.0", "v4.2.0", "4.1.0-beta"})
    void getFile_validVersion_isInHomeFolder(String version) {
        File file = repo.getFile(version, KosmosUtils.DOWNLOAD_JAR_FILE_NAME);
        assertEquals(tempDir.toAbsolutePath().normalize(), file.toPath().toAbsolutePath().normalize().getParent());
        assertEquals("integrasjonspunkt-" + version + ".jar", file.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"x/../../tmp/test", "../../../evil", "sub/4.2.0"})
    void getFile_versionOutsideHomeFolder_throws(String version) {
        assertThrows(IllegalArgumentException.class, () -> repo.getFile(version, KosmosUtils.DOWNLOAD_JAR_FILE_NAME));
        assertThrows(IllegalArgumentException.class, () -> repo.getFile(version, KosmosUtils.UNVALIDATED_JAR_FILE_NAME));
    }

}
