package no.difi.move.deploymanager.action.application;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import lombok.SneakyThrows;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.NexusRepo;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.zeroturnaround.exec.InvalidExitValueException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ValidateAction.class, IOUtils.class, MessageDigest.class, ByteArrayUtil.class})
public class ValidateActionTest {

    private static final byte[] CHECKSUM = "theChecksum".getBytes();

    @InjectMocks private ValidateAction target;

    @Mock private NexusRepo nexusRepoMock;
    //@Mock private JarsSignerService jarsSignerService;
    //TODO rewrite test to use gpgService
    @Mock private InputStream inputStreamMock;
    @Mock private MessageDigest digestMock;
    @Mock private StringWriter stringWriterMock;
    @Mock private File fileMock;
    @Mock private FileInputStream fileInputStreamMock;
    @Mock private DigestInputStream digestInputStreamMock;
    @Mock private InvalidExitValueException invalidExitValueExceptionMock;

    @Spy private Application application = new Application();

    @Before
    @SneakyThrows
    public void before() {
        application.setLatest(new ApplicationMetadata()
                .setVersion("version")
                .setFile(fileMock)
        );

        given(fileMock.getAbsolutePath()).willReturn("jarPath");
        given(nexusRepoMock.getChecksum(anyString(), anyString())).willReturn(CHECKSUM);

        whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInputStreamMock);
        whenNew(DigestInputStream.class).withAnyArguments().thenReturn(digestInputStreamMock);
        given(digestInputStreamMock.read(any())).willReturn(-1);

        mockStatic(MessageDigest.class);
        given(MessageDigest.getInstance(anyString())).willReturn(digestMock);
        given(MessageDigest.isEqual(any(), any())).willReturn(true);
    }

    @Test(expected = DeployActionException.class)
    public void apply_verificationFails_shouldThrow() {
        given(MessageDigest.isEqual(any(), any())).willReturn(false);
        target.apply(application);
    }

    @Test
    public void apply_ExceptionCaught_shouldThrow() throws Exception {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_GATEWAY, "Bad gateway");
        given(nexusRepoMock.getChecksum(anyString(), anyString())).willThrow(exception);
        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(DeployActionException.class)
                .hasMessage("Error validating jar")
                .hasCause(exception);
    }

    @Test(expected = DeployActionException.class)
    public void apply_NoSuchAlgorithmExceptionCaught_shouldThrow() throws Exception {
        given(MessageDigest.getInstance(anyString())).willThrow(new NoSuchAlgorithmException("test exception"));
        target.apply(application);
    }

    //TODO add tests for gpg verification
    /*
    @Test
    public void apply_jarSigningVerificationFails_shouldThrow() {
        doThrow(invalidExitValueExceptionMock).when(jarsSignerService).verify(any());
        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(DeployActionException.class)
                .hasMessage("Error validating jar")
                .hasCause(invalidExitValueExceptionMock);
        verify(jarsSignerService).verify("jarPath");
    }

    @Test
    public void apply_verificationSucceeds_shouldSucceed() {
        assertThat(target.apply(application)).isSameAs(application);
        verify(jarsSignerService).verify("jarPath");
        verify(nexusRepoMock).getChecksum("version", "jar.sha1");
    } */
}
