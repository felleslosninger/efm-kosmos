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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.whenNew;

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
    @Mock private Path pathMock;

    private Application application;

    @Before
    @SneakyThrows
    public void before() {
        application = new Application()
                .setCurrent(new ApplicationMetadata().setVersion(OLDER_APPLICATION_VERSION))
                .setLatest(new ApplicationMetadata().setVersion(NEW_APPLICATION_VERSION));

        given(propertiesMock.getHome()).willReturn("");

        whenNew(File.class).withParameterTypes(String.class, String.class)
                .withArguments(Mockito.anyString(), Mockito.anyString())
                .thenReturn(fileMock);

        given(fileMock.toPath()).willReturn(pathMock);
    }

    @Test(expected = NullPointerException.class)
    public void apply_toNull_shouldThrow() {
        target.apply(null);
    }

    @Test
    public void apply_newVersionFound_shouldDownload() {
        assertThat(target.apply(application)).isSameAs(application);

        File resultFile = application.getLatest().getFile();
        assertThat(resultFile).isSameAs(fileMock);

        verify(nexusRepoMock).downloadJAR(eq(NEW_APPLICATION_VERSION), same(pathMock));
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

    @Test
    public void apply_downLoadFails_shouldThrow() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Download failed!");
        doThrow(exception).when(nexusRepoMock).downloadJAR(any(), any());
        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(DeployActionException.class)
                .hasMessage("Error getting latest version")
                .hasCause(exception);

        verify(nexusRepoMock).downloadJAR(eq(NEW_APPLICATION_VERSION), same(pathMock));
    }
}
