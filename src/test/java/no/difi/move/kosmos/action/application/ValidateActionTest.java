package no.difi.move.kosmos.action.application;

import lombok.SneakyThrows;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.MavenCentralRepo;
import no.difi.move.kosmos.service.codesigner.GpgService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ValidateActionTest {

    private static final byte[] CHECKSUM = "theChecksum".getBytes();

    @InjectMocks
    private ValidateAction target;

    @Mock
    private MavenCentralRepo mavenCentralRepoMock;
    @Mock
    private GpgService gpgService;
    @Mock
    private File fileMock;

    @Spy
    private final Application application = new Application();

    @BeforeAll
    public static void beforeAll() {
        mockStatic(MessageDigest.class);
    }

    @BeforeEach
    @SneakyThrows
    public void before() {
        application.setLatest(new ApplicationMetadata()
                .setVersion("version")
                .setFile(fileMock)
        );
        application.setMarkedForValidation(true);
        given(MessageDigest.isEqual(any(), any())).willReturn(true);
    }

    @Test
    public void apply_verificationFails_shouldThrow() {
        given(MessageDigest.isEqual(any(), any())).willReturn(false);
        given(mavenCentralRepoMock.getChecksum(anyString(), anyString())).willReturn(CHECKSUM);
        assertThrows(KosmosActionException.class, () -> target.apply(application));
    }

    @Test
    public void apply_ExceptionCaught_shouldThrow() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_GATEWAY, "Bad gateway");
        given(mavenCentralRepoMock.getChecksum(anyString(), anyString())).willThrow(exception);
        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("Error validating jar")
                .hasCause(exception);
    }

    @Test
    public void apply_NoSuchAlgorithmExceptionCaught_shouldThrow() throws Exception {
        given(MessageDigest.getInstance(anyString())).willThrow(new NoSuchAlgorithmException("test exception"));
        assertThrows(KosmosActionException.class, () -> target.apply(application));
    }

    @Test
    public void apply_gpgSigningVerificationFails_shouldThrow() {
        given(mavenCentralRepoMock.getChecksum(anyString(), anyString())).willReturn(CHECKSUM);
        assertThrows(KosmosActionException.class, () -> target.apply(application));
    }

    @Test
    public void apply_NoNewDistributionHasBeenDownloaded_ShouldNotValidate() {
        application.setMarkedForValidation(false);

        target.apply(application);

        verify(mavenCentralRepoMock, never()).downloadSignature(anyString());
        verify(gpgService, never()).verify(anyString(), anyString());
    }
}
