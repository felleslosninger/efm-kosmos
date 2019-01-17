package no.difi.move.deploymanager.action.application;

import lombok.SneakyThrows;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.repo.NexusRepo;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PrepareApplicationAction.class, IOUtils.class})
public class PrepareApplicationActionTest {

    private static final String NEW_APPLICATION_VERSION = "newVersion";
    private static final String OLDER_APPLICATION_VERSION = "olderVersion";

    @InjectMocks private PrepareApplicationAction target;

    @Mock private DeployManagerProperties propertiesMock;
    @Mock private NexusRepo nexusRepoMock;
    @Mock private DeployDirectoryRepo deployDirectoryRepoMock;
    @Mock private File fileMock;
    @Mock private File blackListedFileMock;
    @Mock private URL urlMock;
    @Mock private InputStream streamMock;

    private Application application;

    @Before
    @SneakyThrows
    public void before() {
        application = new Application()
                .setCurrent(new ApplicationMetadata().setVersion(OLDER_APPLICATION_VERSION))
                .setLatest(new ApplicationMetadata().setVersion(NEW_APPLICATION_VERSION));

        when(propertiesMock.getRoot()).thenReturn("");

        whenNew(File.class).withParameterTypes(String.class, String.class)
                .withArguments(Mockito.anyString(), Mockito.anyString())
                .thenReturn(fileMock);
    }

    @Test(expected = NullPointerException.class)
    public void apply_toNull_shouldThrow() {
        target.apply(null);
    }

    @Test
    public void apply_newVersionFound_shouldDownload() throws Exception {
        doSuccessfulDownload();

        assertThat(target.apply(application)).isSameAs(application);

        File resultFile = application.getLatest().getFile();
        assertThat(resultFile).isSameAs(fileMock);
    }

    @Test(expected = DeployActionException.class)
    public void apply_downloadThrows_shouldThrow() throws Exception {
        getDownloadException();
        target.apply(application);
    }

    @Test
    public void apply_isBlackListed_shouldThrow() {
        given(deployDirectoryRepoMock.isBlackListed(any())).willReturn(true);
        given(deployDirectoryRepoMock.getBlackListedFile(any())).willReturn(blackListedFileMock);
        given(blackListedFileMock.getAbsolutePath()).willReturn("/tmp/test.jar.blacklisted");

        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(DeployActionException.class)
                .hasMessage("The latest version is black listed! Remove /tmp/test.jar.blacklisted to white list.")
                .hasNoCause();
    }

    private void doSuccessfulDownload() throws Exception {
        when(urlMock.openStream()).thenReturn(streamMock);
        when(nexusRepoMock.getArtifact(Mockito.anyString(), Mockito.any())).thenReturn(urlMock);
        mockStatic(IOUtils.class);
        whenNew(FileOutputStream.class).withParameterTypes(File.class)
                .withArguments(null)
                .thenReturn(PowerMockito.mock(FileOutputStream.class));
        when(IOUtils.copy(Mockito.any(streamMock.getClass()), Mockito.any(OutputStream.class))).thenReturn(1);
    }

    private void getDownloadException() throws Exception {
        when(urlMock.openStream()).thenReturn(streamMock);
        when(nexusRepoMock.getArtifact(Mockito.anyString(), Mockito.any()))
                .thenThrow(new MalformedURLException("test download exception"));
    }
}
