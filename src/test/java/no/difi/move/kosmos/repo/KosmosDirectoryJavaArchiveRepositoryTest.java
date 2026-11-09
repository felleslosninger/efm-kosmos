package no.difi.move.kosmos.repo;

import no.difi.move.kosmos.config.BlocklistProperties;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.util.KosmosUtils;
import org.junit.jupiter.api.Assumptions;
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
    void getAllowlistVersion_noAllowlistFiles_returnsNull() {
        assertNull(repo.getAllowlistVersion());
    }

    @Test
    void getAllowlistVersion_returnsHighestSemanticVersion() throws IOException {
        for (String version : new String[]{"2.9.0", "2.10.0-SNAPSHOT", "2.10.0", "2.2.1", "2.10.0-beta"}) {
            Files.createFile(tempDir.resolve("integrasjonspunkt-%s.allowlisted".formatted(version)));
        }
        // compared numerically, not as text, and a pre-release is lower than the release
        assertEquals("2.10.0", repo.getAllowlistVersion());
    }

    @Test
    void getAllowlistVersion_namesWithoutValidVersionAreIgnored() throws IOException {
        Files.createFile(tempDir.resolve("integrasjonspunkt-2.9.0.allowlisted"));
        Files.createFile(tempDir.resolve("something.allowlisted"));
        Files.createFile(tempDir.resolve("integrasjonspunkt-3.0.allowlisted"));
        assertEquals("2.9.0", repo.getAllowlistVersion());
    }

    @Test
    void getAllowlistVersion_homeFolderMissing_createsFolderAndReturnsNull() {
        // the parent is missing too, File.mkdir() could not create this
        Path home = tempDir.resolve("missing").resolve("home");
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(home.toString());

        assertNull(repo.getAllowlistVersion());
        assertTrue(Files.isDirectory(home));
    }

    @Test
    void getFile_homeFolderMissing_createsFolder() {
        Path home = tempDir.resolve("missing").resolve("home");
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(home.toString());

        File file = repo.getFile("2.31.0", KosmosUtils.DOWNLOAD_JAR_FILE_NAME);

        assertTrue(Files.isDirectory(home));
        assertEquals(home.toAbsolutePath().normalize(), file.toPath().getParent());
    }

    @Test
    void getAllowlistVersion_homeIsAFile_throwsWithPath() throws IOException {
        Path home = Files.createFile(tempDir.resolve("home"));
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(home.toString());

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> repo.getAllowlistVersion());
        assertEquals("The home folder %s is not a folder".formatted(home.toAbsolutePath()), e.getMessage());
    }

    @Test
    void getAllowlistVersion_parentIsAFile_throwsWithPath() throws IOException {
        Path parent = Files.createFile(tempDir.resolve("parent"));
        Path home = parent.resolve("home");
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(home.toString());

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> repo.getAllowlistVersion());
        assertTrue(e.getMessage().startsWith("Could not create the home folder %s".formatted(home.toAbsolutePath())), e.getMessage());
    }

    @Test
    void getAllowlistVersion_homeNotReadable_throwsWithPath() throws IOException {
        Path home = Files.createDirectory(tempDir.resolve("home"));
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(home.toString());
        // Windows ignores the read flag on folders, and root can read anyway
        Assumptions.assumeTrue(home.toFile().setReadable(false) && !Files.isReadable(home), "Could not make the folder unreadable");
        try {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> repo.getAllowlistVersion());
            assertEquals("The home folder %s cannot be read".formatted(home.toAbsolutePath()), e.getMessage());
        } finally {
            assertTrue(home.toFile().setReadable(true));
        }
    }

    @Test
    void getAllowlistVersion_preReleaseIsHigherThanPreviousRelease() throws IOException {
        Files.createFile(tempDir.resolve("integrasjonspunkt-4.0.0.allowlisted"));
        Files.createFile(tempDir.resolve("integrasjonspunkt-4.0.1-beta.allowlisted"));
        assertEquals("4.0.1-beta", repo.getAllowlistVersion());
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

    @Test
    void getFile_symlinkFollowedByParent_staysInHomeFolder() throws IOException {
        Path home = Files.createDirectory(tempDir.resolve("home"));
        Path outside = Files.createDirectories(tempDir.resolve("outside").resolve("sub"));
        try {
            Files.createSymbolicLink(home.resolve("integrasjonspunkt-x"), outside);
        } catch (IOException | UnsupportedOperationException e) {
            // creating symlinks needs extra privileges on Windows
            Assumptions.abort("Could not create symlink: " + e.getMessage());
        }
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(home.toString());

        // through the symlink this would be outside/evil.jar
        File file = repo.getFile("x/../evil", KosmosUtils.DOWNLOAD_JAR_FILE_NAME);

        assertEquals(home.toAbsolutePath().normalize().resolve("evil.jar"), file.toPath());
        assertEquals(home.toRealPath(), file.getCanonicalFile().toPath().getParent());
    }

}
