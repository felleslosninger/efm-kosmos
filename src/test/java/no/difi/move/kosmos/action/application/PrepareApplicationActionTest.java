package no.difi.move.kosmos.action.application;

import lombok.SneakyThrows;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.BlocklistProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryRepo;
import no.difi.move.kosmos.repo.MavenCentralRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrepareApplicationActionTest {

    private static final String NEW_APPLICATION_VERSION = "newVersion";
    private static final String OLDER_APPLICATION_VERSION = "olderVersion";

    @InjectMocks
    private PrepareApplicationAction target;

    @Mock
    private KosmosProperties propertiesMock;
    @Mock
    private MavenCentralRepo mavenCentralRepoMock;
    @Mock
    private KosmosDirectoryRepo kosmosDirectoryRepoMock;

    @Mock
    private File fileMock;
    @Mock
    private File blockListedFileMock;
    private Path pathMock;
    @Mock
    private BlocklistProperties blocklistPropertiesMock;
    private Application application;

    @BeforeEach
    @SneakyThrows
    public void before() {
        application = new Application()
                .setCurrent(new ApplicationMetadata().setVersion(OLDER_APPLICATION_VERSION))
                .setLatest(new ApplicationMetadata().setVersion(NEW_APPLICATION_VERSION));
    }

    @Test
    public void apply_ApplicationIsNull_ShouldThrow() {
        assertThrows(NullPointerException.class,
                () -> target.apply(null)
        );
    }

    @Test
    public void apply_NewVersionFound_ShouldDownload() {
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(fileMock.exists()).willReturn(false);
        given(fileMock.toPath()).willReturn(pathMock);
        given(kosmosDirectoryRepoMock.getFile(anyString(), anyString())).willReturn(fileMock);

        final Application result = target.apply(application);

        assertThat(result.isMarkedForValidation()).isTrue();
        assertThat(result).isSameAs(application);
        File resultFile = application.getLatest().getFile();
        assertThat(resultFile).isSameAs(fileMock);
        verify(mavenCentralRepoMock).downloadJAR(eq(NEW_APPLICATION_VERSION), same(pathMock));
    }

    @Test
    public void apply_NewVersionIsBlockListed_ShouldThrow() {
        given(blocklistPropertiesMock.isEnabled()).willReturn(true);
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(kosmosDirectoryRepoMock.isBlockListed(any())).willReturn(true);
        given(kosmosDirectoryRepoMock.getBlocklistPath(any())).willReturn(blockListedFileMock);
        given(kosmosDirectoryRepoMock.getFile(anyString(), anyString())).willReturn(fileMock);
        given(kosmosDirectoryRepoMock.isBlockListed(any(File.class))).willReturn(true);
        given(blockListedFileMock.getAbsolutePath()).willReturn("/tmp/test.jar.blocklisted");

        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("The latest version is block listed! Remove /tmp/test.jar.blocklisted to allow version.")
                .hasNoCause();
    }

    @Test
    public void apply_NewVersionIsBlockListedAndBlocklistIsDisabled_ShouldNotThrow() {
        given(blocklistPropertiesMock.isEnabled()).willReturn(false);
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(kosmosDirectoryRepoMock.getFile(anyString(), anyString())).willReturn(fileMock);

        assertDoesNotThrow(() -> target.apply(application));
    }

    @Test
    public void apply_DownLoadFails_ShouldThrow() {
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(fileMock.exists()).willReturn(false);
        given(fileMock.toPath()).willReturn(pathMock);
        given(kosmosDirectoryRepoMock.getFile(anyString(), anyString())).willReturn(fileMock);
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Download failed!");
        doThrow(exception).when(mavenCentralRepoMock).downloadJAR(any(), any());

        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("Error occurred when downloading latest version")
                .hasCause(exception);

        verify(mavenCentralRepoMock).downloadJAR(eq(NEW_APPLICATION_VERSION), same(pathMock));
    }

    @Test
    public void apply_NoNewVersionIsDownloaded_ShouldNotDownload() {
        given(blocklistPropertiesMock.isEnabled()).willReturn(true);
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(fileMock.exists()).willReturn(true);
        given(kosmosDirectoryRepoMock.getFile(anyString(), anyString())).willReturn(fileMock);

        assertThat(target.apply(application)).isSameAs(application);

        File resultFile = application.getLatest().getFile();

        assertThat(resultFile).isSameAs(fileMock);
        verify(mavenCentralRepoMock, never()).downloadJAR(anyString(), any());
    }
}
