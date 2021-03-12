package no.difi.move.deploymanager.repo;

import no.difi.move.deploymanager.config.BlacklistProperties;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.IntegrasjonspunktProperties;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DeployDirectoryRepo.class, FileUtils.class})
public class DeployDirectoryRepoTest {

    @InjectMocks
    private DeployDirectoryRepo target;

    @Mock
    private DeployManagerProperties properties;
    @Mock
    private File file;
    @Mock
    private File blacklistedFile;

    @Rule
    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        temporaryFolder.create();
        BlacklistProperties blacklistProperties = mock(BlacklistProperties.class);
        when(blacklistProperties.getDurationInHours()).thenReturn(2);
        when(properties.getBlacklist()).thenReturn(blacklistProperties);
        when(file.getAbsolutePath()).thenReturn(temporaryFolder.getRoot().getAbsolutePath(), "file.jar");
        when(file.getName()).thenReturn("application");
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

        target.getFile("version-1");

        verifyNew(File.class).withArguments(temporaryFolder.getRoot().getAbsolutePath());
        verify(directoryMock).mkdir();
        verifyNew(File.class).withArguments(directoryMock, "integrasjonspunkt-version-1.jar");
    }

    @Test
    public void blacklist_FileProvided_BlacklistFileShouldBeCreated() throws Exception {
        whenNew(File.class).withAnyArguments().thenReturn(blacklistedFile);
        when(blacklistedFile.toPath())
                .thenReturn(Paths.get(temporaryFolder.getRoot().getAbsolutePath(), "file.jar"));
        when(blacklistedFile.createNewFile()).thenReturn(true);
        BufferedWriter writer = mock(BufferedWriter.class);

        whenNew(BufferedWriter.class).withAnyArguments().thenReturn(writer);

        target.blackList(file);

        verify(blacklistedFile).createNewFile();
    }

    @Test
    public void isBlacklisted_FileIsNotBlacklisted_ShouldReturnFalse() throws Exception {
        whenNew(File.class).withAnyArguments().thenReturn(blacklistedFile);
        when(blacklistedFile.exists()).thenReturn(false);
        mockStatic(FileUtils.class);

        assertFalse(target.isBlackListed(file));
        PowerMockito.verifyZeroInteractions(FileUtils.class);
    }

    @Test
    public void isBlacklisted_BlacklistIsNotExpired_ShouldReturnTrue() throws Exception {
        whenNew(File.class).withAnyArguments().thenReturn(blacklistedFile);
        mockStatic(FileUtils.class);
        when(blacklistedFile.exists()).thenReturn(true);
        LocalDateTime expires = LocalDateTime.now().plusMinutes(2);
        PowerMockito.when(FileUtils.readFileToString(any(File.class), any(Charset.class)))
                .thenReturn(expires.toString());

        assertTrue(target.isBlackListed(file));

        PowerMockito.verifyStatic(FileUtils.class);
        FileUtils.readFileToString(any(), any(Charset.class));
        PowerMockito.verifyStatic(FileUtils.class, never());
        FileUtils.deleteQuietly(any(File.class));
    }

    @Test
    public void isBlacklisted_BlacklistIsExpired_ShouldReturnFalse() throws Exception {
        whenNew(File.class).withAnyArguments().thenReturn(blacklistedFile);
        mockStatic(FileUtils.class);
        when(blacklistedFile.exists()).thenReturn(true);
        LocalDateTime expires = LocalDateTime.now().minusSeconds(1);
        PowerMockito.when(FileUtils.readFileToString(any(File.class), any(Charset.class)))
                .thenReturn(expires.toString());

        assertFalse(target.isBlackListed(file));

        PowerMockito.verifyStatic(FileUtils.class);
        FileUtils.readFileToString(any(), any(Charset.class));
        FileUtils.deleteQuietly(blacklistedFile);
    }
}