package no.difi.move.kosmos.action.application;

import lombok.SneakyThrows;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.BlocklistProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryJavaArchiveRepository;
import no.difi.move.kosmos.repo.JavaArchiveRepository;
import no.difi.move.kosmos.service.codesigner.JarVerifier;
import no.difi.move.kosmos.util.KosmosUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.nio.file.Files;
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
    private JavaArchiveRepository javaArchiveRepositoryMock;
    @Mock
    private KosmosDirectoryJavaArchiveRepository kosmosDirectoryJavaArchiveRepositoryMock;
    @Mock
    private JarVerifier jarVerifierMock;

    @Mock
    private File fileMock;
    @Mock
    private File unvalidatedFileMock;
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
    public void apply_NewVersionFound_ShouldDownloadToUnvalidatedJarFile() {
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(fileMock.exists()).willReturn(false);
        given(unvalidatedFileMock.toPath()).willReturn(pathMock);
        given(kosmosDirectoryJavaArchiveRepositoryMock.getFile(NEW_APPLICATION_VERSION, KosmosUtils.DOWNLOAD_JAR_FILE_NAME)).willReturn(fileMock);
        given(kosmosDirectoryJavaArchiveRepositoryMock.getFile(NEW_APPLICATION_VERSION, KosmosUtils.UNVALIDATED_JAR_FILE_NAME)).willReturn(unvalidatedFileMock);

        final Application result = target.apply(application);

        assertThat(result.isMarkedForValidation()).isTrue();
        assertThat(result).isSameAs(application);
        File resultFile = application.getLatest().getFile();
        assertThat(resultFile).isSameAs(unvalidatedFileMock);
        verify(javaArchiveRepositoryMock).downloadJAR(eq(NEW_APPLICATION_VERSION), same(pathMock));
    }

    @Test
    public void apply_NewVersionIsBlockListed_ShouldThrow() {
        given(blocklistPropertiesMock.isEnabled()).willReturn(true);
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(kosmosDirectoryJavaArchiveRepositoryMock.isBlockListed(any())).willReturn(true);
        given(kosmosDirectoryJavaArchiveRepositoryMock.getBlocklistPath(any())).willReturn(blockListedFileMock);
        given(kosmosDirectoryJavaArchiveRepositoryMock.getFile(anyString(), anyString())).willReturn(fileMock);
        given(kosmosDirectoryJavaArchiveRepositoryMock.isBlockListed(any(File.class))).willReturn(true);
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
        given(kosmosDirectoryJavaArchiveRepositoryMock.getFile(anyString(), anyString())).willReturn(fileMock);

        assertDoesNotThrow(() -> target.apply(application));
    }

    @Test
    @SneakyThrows
    public void apply_DownLoadFails_ShouldDeletePartialDownloadAndThrow(@TempDir Path home) {
        File unvalidatedFile = home.resolve("integrasjonspunkt-newVersion.jar.download").toFile();
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(fileMock.exists()).willReturn(false);
        given(kosmosDirectoryJavaArchiveRepositoryMock.getFile(NEW_APPLICATION_VERSION, KosmosUtils.DOWNLOAD_JAR_FILE_NAME)).willReturn(fileMock);
        given(kosmosDirectoryJavaArchiveRepositoryMock.getFile(NEW_APPLICATION_VERSION, KosmosUtils.UNVALIDATED_JAR_FILE_NAME)).willReturn(unvalidatedFile);
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Download failed!");
        doAnswer(invocation -> {
            Files.writeString(invocation.getArgument(1), "partial");
            throw exception;
        }).when(javaArchiveRepositoryMock).downloadJAR(any(), any());

        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("Error occurred when downloading latest version")
                .hasCause(exception);

        verify(javaArchiveRepositoryMock).downloadJAR(eq(NEW_APPLICATION_VERSION), eq(unvalidatedFile.toPath()));
        assertThat(unvalidatedFile).doesNotExist();
        assertThat(application.isMarkedForValidation()).isFalse();
    }

    @Test
    public void apply_NoNewVersionIsDownloaded_ShouldNotDownload() {
        given(blocklistPropertiesMock.isEnabled()).willReturn(true);
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(fileMock.exists()).willReturn(true);
        given(kosmosDirectoryJavaArchiveRepositoryMock.getFile(anyString(), anyString())).willReturn(fileMock);
        given(jarVerifierMock.isVerified(NEW_APPLICATION_VERSION, fileMock)).willReturn(true);

        assertThat(target.apply(application)).isSameAs(application);

        File resultFile = application.getLatest().getFile();

        assertThat(resultFile).isSameAs(fileMock);
        assertThat(application.isMarkedForValidation()).isFalse();
        verify(javaArchiveRepositoryMock, never()).downloadJAR(anyString(), any());
    }

    @Test
    public void apply_ExistingJarIsNotVerified_ShouldDownloadAgain() {
        given(propertiesMock.getBlocklist()).willReturn(blocklistPropertiesMock);
        given(fileMock.exists()).willReturn(true);
        given(unvalidatedFileMock.toPath()).willReturn(pathMock);
        given(kosmosDirectoryJavaArchiveRepositoryMock.getFile(NEW_APPLICATION_VERSION, KosmosUtils.DOWNLOAD_JAR_FILE_NAME)).willReturn(fileMock);
        given(kosmosDirectoryJavaArchiveRepositoryMock.getFile(NEW_APPLICATION_VERSION, KosmosUtils.UNVALIDATED_JAR_FILE_NAME)).willReturn(unvalidatedFileMock);
        given(jarVerifierMock.isVerified(NEW_APPLICATION_VERSION, fileMock)).willReturn(false);

        assertThat(target.apply(application)).isSameAs(application);

        // the download is validated and then replaces the existing jar
        assertThat(application.isMarkedForValidation()).isTrue();
        assertThat(application.getLatest().getFile()).isSameAs(unvalidatedFileMock);
        verify(javaArchiveRepositoryMock).downloadJAR(eq(NEW_APPLICATION_VERSION), same(pathMock));
    }
}
