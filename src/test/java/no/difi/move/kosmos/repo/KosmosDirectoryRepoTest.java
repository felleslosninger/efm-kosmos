package no.difi.move.kosmos.repo;

import no.difi.move.kosmos.config.BlocklistProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.util.KosmosUtils;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KosmosDirectoryRepo.class, FileUtils.class})
public class KosmosDirectoryRepoTest {

    @InjectMocks
    private KosmosDirectoryRepo target;

    @Mock
    private KosmosProperties properties;
    @Mock
    private File file;
    @Mock
    private File blocklistedFile;
    @Mock
    private File allowlistedFile;

    @Rule
    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        temporaryFolder.create();
        BlocklistProperties blocklistProperties = mock(BlocklistProperties.class);
        when(blocklistProperties.getDurationInHours()).thenReturn(2);
        when(properties.getBlocklist()).thenReturn(blocklistProperties);
        when(file.getAbsolutePath()).thenReturn(temporaryFolder.getRoot().getAbsolutePath(), "file.jar");
        when(file.getName()).thenReturn("application");
        IntegrasjonspunktProperties integrasjonspunktProperties = mock(IntegrasjonspunktProperties.class);
        when(properties.getIntegrasjonspunkt()).thenReturn(integrasjonspunktProperties);
    }

    @After
    public void tearDown() {
        temporaryFolder.delete();
    }

    @Test
    public void getFile_Success_VerifyInteractions() throws Exception {
        when(properties.getIntegrasjonspunkt())
                .thenReturn(new IntegrasjonspunktProperties()
                        .setHome(temporaryFolder.getRoot().getAbsolutePath()));
        File directoryMock = mock(File.class);
        whenNew(File.class).withAnyArguments().thenReturn(directoryMock);

        target.getFile("version-1", KosmosUtils.DOWNLOAD_JAR_FILE_NAME);

        verifyNew(File.class).withArguments(temporaryFolder.getRoot().getAbsolutePath());
        verify(directoryMock).mkdir();
        verifyNew(File.class).withArguments(directoryMock, "integrasjonspunkt-version-1.jar");
    }

    @Test
    public void blocklist_FileProvided_BlocklistFileShouldBeCreated() throws Exception {
        whenNew(File.class).withAnyArguments().thenReturn(blocklistedFile);
        when(blocklistedFile.toPath())
                .thenReturn(Paths.get(temporaryFolder.getRoot().getAbsolutePath(), "file.jar"));
        when(blocklistedFile.createNewFile()).thenReturn(true);
        BufferedWriter writer = mock(BufferedWriter.class);

        whenNew(BufferedWriter.class).withAnyArguments().thenReturn(writer);

        target.blockList(file);

        verify(blocklistedFile).createNewFile();
    }

    @Test
    public void isBlocklisted_FileIsNotBlocklisted_ShouldReturnFalse() throws Exception {
        whenNew(File.class).withAnyArguments().thenReturn(blocklistedFile);
        when(blocklistedFile.exists()).thenReturn(false);
        mockStatic(FileUtils.class);

        assertFalse(target.isBlockListed(file));
        PowerMockito.verifyZeroInteractions(FileUtils.class);
    }

    @Test
    public void isBlacklisted_BlacklistIsNotExpired_ShouldReturnTrue() throws Exception {
        whenNew(File.class).withAnyArguments().thenReturn(blocklistedFile);
        mockStatic(FileUtils.class);
        when(blocklistedFile.exists()).thenReturn(true);
        LocalDateTime expires = LocalDateTime.now().plusMinutes(2);
        PowerMockito.when(FileUtils.readFileToString(any(File.class), any(Charset.class)))
                .thenReturn(expires.toString());

        assertTrue(target.isBlockListed(file));

        PowerMockito.verifyStatic(FileUtils.class);
        FileUtils.readFileToString(any(), any(Charset.class));
        PowerMockito.verifyStatic(FileUtils.class, never());
        FileUtils.deleteQuietly(any(File.class));
    }

    @Test
    public void isBlocklisted_BlocklistIsExpired_ShouldReturnFalse() throws Exception {
        whenNew(File.class).withAnyArguments().thenReturn(blocklistedFile);
        mockStatic(FileUtils.class);
        when(blocklistedFile.exists()).thenReturn(true);
        LocalDateTime expires = LocalDateTime.now().minusSeconds(1);
        PowerMockito.when(FileUtils.readFileToString(any(File.class), any(Charset.class)))
                .thenReturn(expires.toString());

        assertFalse(target.isBlockListed(file));

        PowerMockito.verifyStatic(FileUtils.class);
        FileUtils.readFileToString(any(), any(Charset.class));
        FileUtils.deleteQuietly(blocklistedFile);
    }

    @Test
    public void getAllowlistFile_shouldSucceed() throws IOException {
        temporaryFolder.newFile("integrasjonspunkt-1.1.11.allowlisted");
        temporaryFolder.newFile("integrasjonspunkt-1.2.1.allowlisted");
        temporaryFolder.newFile("integrasjonspunkt-2.0.12.allowlisted");
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

        assertEquals("integrasjonspunkt-2.0.12.allowlisted", target.getAllowlistFile().getName());
    }

    @Test
    public void getAllowlistFile_noFileFound_shouldReturnNull() {
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        assertNull(target.getAllowlistFile());
    }

    @Test
    public void getAllowVersion_shouldReturnSemanticVersion() throws IOException {
        temporaryFolder.newFile("integrasjonspunkt-1.1.11.allowlisted");
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        assertEquals("1.1.11", target.getAllowlistVersion());
    }

    @Test
    public void getAllowVersion_noFileFound_shouldReturnNull() {
        when(properties.getIntegrasjonspunkt().getHome()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        assertNull(target.getAllowlistVersion());
    }

    @Test
    public void removeAllowlistFile_shouldSucceed() throws Exception {
        String version = "1.1.11";
        whenNew(File.class).withAnyArguments().thenReturn(allowlistedFile);
        mockStatic(FileUtils.class);
        when(allowlistedFile.exists()).thenReturn(true);
        target.removeAllowlist(version);
        PowerMockito.verifyStatic(FileUtils.class);
        FileUtils.deleteQuietly(allowlistedFile);
    }

    @Test
    public void allowlist_FileProvided_AllowlistFileShouldBeCreated() throws Exception {
        whenNew(File.class).withAnyArguments().thenReturn(allowlistedFile);
        when(allowlistedFile.toPath())
                .thenReturn(Paths.get(temporaryFolder.getRoot().getAbsolutePath(), "file.jar"));
        when(allowlistedFile.createNewFile()).thenReturn(true);
        BufferedWriter writer = mock(BufferedWriter.class);
        whenNew(BufferedWriter.class).withAnyArguments().thenReturn(writer);
        target.allowlist(file, "1.2.1");

        verify(allowlistedFile).createNewFile();
    }
}