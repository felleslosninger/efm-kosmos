package no.difi.move.deploymanager.action.application;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import lombok.SneakyThrows;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.NexusRepo;
import no.difi.move.deploymanager.service.codesigner.GpgService;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
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

import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ValidateAction.class, IOUtils.class, MessageDigest.class, ByteArrayUtil.class})
public class ValidateActionTest {

    private static final byte[] CHECKSUM = "theChecksum".getBytes();

    @InjectMocks
    private ValidateAction target;

    @Mock
    private NexusRepo nexusRepoMock;
    @Mock
    private GpgService gpgService;
    @Mock
    private MessageDigest digestMock;
    @Mock
    private File fileMock;
    @Mock
    private FileInputStream fileInputStreamMock;
    @Mock
    private DigestInputStream digestInputStreamMock;

    @Spy
    private final Application application = new Application();

    private String signature;

    @Before
    @SneakyThrows
    public void before() {
        application.setLatest(new ApplicationMetadata()
                .setVersion("version")
                .setFile(fileMock)
        );

        signature = "signature";
        given(fileMock.getAbsolutePath()).willReturn("jarPath");
        given(nexusRepoMock.getChecksum(anyString(), anyString())).willReturn(CHECKSUM);

        given(nexusRepoMock.downloadSignature(application.getLatest().getVersion())).willReturn(signature);

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

    @Test
    public void apply_gpgSigningVerificationSuccess_shouldSucceed() {
        Assertions.assertThat(target.apply(application)).isSameAs(application);
        verify(gpgService).verify("jarPath", signature);
        verify(nexusRepoMock).getChecksum("version", "jar.sha1");
    }

    @Test
    public void apply_gpgSigningVerificationFails_shouldThrow() {
        given(nexusRepoMock.downloadSignature(application.getLatest().getVersion())).willReturn("tull");
        doThrow(DeployActionException.class).when(gpgService).verify(anyString(), anyString());
        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(DeployActionException.class)
                .hasCause(new DeployActionException(null));
        verify(gpgService).verify("jarPath", "tull");
    }
}
