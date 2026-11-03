package no.difi.move.kosmos.action.application;

import lombok.SneakyThrows;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.BlocklistProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.JavaArchiveRepository;
import no.difi.move.kosmos.repo.KosmosDirectoryJavaArchiveRepository;
import no.difi.move.kosmos.service.codesigner.GpgService;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ValidateActionTest {

    private static final String VERSION = "version";
    private static final byte[] JAR_CONTENT = "theJarContent".getBytes(StandardCharsets.UTF_8);
    private static final String SIGNATURE = "theSignature";

    @InjectMocks
    private ValidateAction target;

    @Mock
    private JavaArchiveRepository javaArchiveRepositoryMock;
    @Mock
    private GpgService gpgService;
    @Mock
    private KosmosDirectoryJavaArchiveRepository deployDirectoryRepoMock;
    @Mock
    private KosmosProperties propertiesMock;

    @TempDir
    private Path home;

    private File unvalidatedJarFile;
    private File jarFile;
    private Application application;

    @BeforeEach
    @SneakyThrows
    public void before() {
        unvalidatedJarFile = home.resolve(KosmosUtils.UNVALIDATED_JAR_FILE_NAME.formatted(VERSION)).toFile();
        jarFile = home.resolve(KosmosUtils.DOWNLOAD_JAR_FILE_NAME.formatted(VERSION)).toFile();
        Files.write(unvalidatedJarFile.toPath(), JAR_CONTENT);
        application = new Application()
                .setLatest(new ApplicationMetadata()
                        .setVersion(VERSION)
                        .setFile(unvalidatedJarFile))
                .setMarkedForValidation(true);
    }

    @Test
    public void apply_validJar_shouldMoveToJarFileName() {
        givenJarFileName();
        givenChecksum(sha1(JAR_CONTENT));
        givenSignatureVerification(true);

        Application result = target.apply(application);

        assertThat(result.getLatest().getFile()).isEqualTo(jarFile);
        assertThat(jarFile).hasBinaryContent(JAR_CONTENT);
        assertThat(unvalidatedJarFile).doesNotExist();
    }

    @Test
    public void apply_signatureVerificationFails_shouldDeleteJarAndThrow() {
        givenJarFileName();
        givenChecksum(sha1(JAR_CONTENT));
        givenSignatureVerification(false);
        givenBlocklistEnabled(false);

        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("Error validating jar")
                .hasRootCauseMessage("Invalid artifact signature");

        assertThat(unvalidatedJarFile).doesNotExist();
        assertThat(jarFile).doesNotExist();
        verify(deployDirectoryRepoMock, never()).blockList(any());
    }

    @Test
    public void apply_signatureVerificationFailsAndBlocklistIsEnabled_shouldBlocklistJarFileName() {
        givenJarFileName();
        givenChecksum(sha1(JAR_CONTENT));
        givenSignatureVerification(false);
        givenBlocklistEnabled(true);

        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class);

        verify(deployDirectoryRepoMock).blockList(jarFile);
        assertThat(unvalidatedJarFile).doesNotExist();
        assertThat(jarFile).doesNotExist();
    }

    @Test
    public void apply_checksumVerificationFails_shouldDeleteJarAndThrow() {
        givenJarFileName();
        givenChecksum(sha1("somethingElse".getBytes(StandardCharsets.UTF_8)));

        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("Error validating jar")
                .hasRootCauseMessage("SHA-1 verification failed");

        assertThat(unvalidatedJarFile).doesNotExist();
        assertThat(jarFile).doesNotExist();
        verify(gpgService, never()).verify(anyString(), anyString());
    }

    @Test
    public void apply_checksumFetchFails_shouldDeleteJarAndThrow() {
        givenJarFileName();
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_GATEWAY, "Bad gateway");
        given(javaArchiveRepositoryMock.getChecksum(anyString(), anyString())).willThrow(exception);

        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("Error validating jar")
                .hasCause(exception);

        assertThat(unvalidatedJarFile).doesNotExist();
        assertThat(jarFile).doesNotExist();
    }

    @Test
    public void apply_NoNewDistributionHasBeenDownloaded_ShouldNotValidate() {
        application.setMarkedForValidation(false);

        target.apply(application);

        verify(javaArchiveRepositoryMock, never()).downloadSignature(anyString());
        verify(gpgService, never()).verify(anyString(), anyString());
    }

    private void givenJarFileName() {
        given(deployDirectoryRepoMock.getFile(VERSION, KosmosUtils.DOWNLOAD_JAR_FILE_NAME)).willReturn(jarFile);
    }

    private void givenChecksum(byte[] checksum) {
        given(javaArchiveRepositoryMock.getChecksum(VERSION, ".sha1")).willReturn(checksum);
    }

    private void givenSignatureVerification(boolean valid) {
        given(javaArchiveRepositoryMock.downloadSignature(VERSION)).willReturn(SIGNATURE);
        given(gpgService.verify(eq(unvalidatedJarFile.getAbsolutePath()), eq(SIGNATURE))).willReturn(valid);
    }

    private void givenBlocklistEnabled(boolean enabled) {
        given(propertiesMock.getBlocklist()).willReturn(new BlocklistProperties().setEnabled(enabled));
    }

    @SneakyThrows
    private static byte[] sha1(byte[] content) {
        return MessageDigest.getInstance("SHA-1").digest(content);
    }
}
