package no.difi.move.deploymanager.service.codesigner;

import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.VerificationProperties;
import org.assertj.core.util.Lists;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GpgServiceImpl.class)
public class GpgServiceImplTest {

    @Mock
    private DeployManagerProperties properties;
    @Mock
    private VerificationProperties verificationProperties;

    @InjectMocks
    private GpgServiceImpl target;

    private static String signedDataFilePath;
    private static String downloadedSignature;
    private static String anotherSignature;
    private static List<String> downloadedPublicKeys;
    private static List<String> noMatchingPublicKeys;
    private static String matchingPublicKeyFilePath;
    private static String notMatchingPublicKeyFilePath;

    @BeforeClass
    public static void beforeClass() throws IOException {
        signedDataFilePath = new ClassPathResource("/gpg/gpgTest.txt").getFile().getAbsolutePath();
        downloadedSignature = new String(readAllBytes(new ClassPathResource("/gpg/signature.asc").getFile().toPath()));
        anotherSignature = new String(readAllBytes(new ClassPathResource("/gpg/gpgTestOtherSignature.txt.asc").getFile().toPath()));
        matchingPublicKeyFilePath = new ClassPathResource("/gpg/public-key.asc").getFile().getAbsolutePath();
        notMatchingPublicKeyFilePath = new ClassPathResource("/gpg/invalidPublicKeyEfmTest.asc").getFile().getAbsolutePath();
    }

    @Before
    public void setUp() {
        when(properties.getVerification()).thenReturn(verificationProperties);
        downloadedPublicKeys = Collections.singletonList(matchingPublicKeyFilePath);
        noMatchingPublicKeys = Collections.singletonList(notMatchingPublicKeyFilePath);
    }

    @Test
    public void verify_Success_ShouldVerifyAndReturnTrue() {
        List<String> bothMatchingAndNotMatchingKeys = Lists.newArrayList(matchingPublicKeyFilePath, notMatchingPublicKeyFilePath);
        when(verificationProperties.getPublicKeyPaths()).thenReturn(bothMatchingAndNotMatchingKeys);
        assertTrue(target.verify(signedDataFilePath, downloadedSignature));
    }

    @Test
    public void verify_WrongPublicKeyInput_ShouldThrow() {
        when(verificationProperties.getPublicKeyPaths()).thenReturn(noMatchingPublicKeys);
        assertThatThrownBy(() -> target.verify(signedDataFilePath, downloadedSignature))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void verify_WrongSignatureInput_ShouldThrow() {
        when(verificationProperties.getPublicKeyPaths()).thenReturn(downloadedPublicKeys);
        assertThatThrownBy(() -> target.verify(signedDataFilePath, anotherSignature))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void verify_InputIsNull_ShouldThrow() {
        when(verificationProperties.getPublicKeyPaths()).thenReturn(downloadedPublicKeys);
        assertThatThrownBy(() -> target.verify(null, downloadedSignature))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void verify_NoSignature_ShouldThrow() throws Exception {
        when(verificationProperties.getPublicKeyPaths()).thenReturn(downloadedPublicKeys);
        JcaPGPObjectFactory objectFactory = mock(JcaPGPObjectFactory.class);
        when(objectFactory.nextObject()).thenReturn(null);
        whenNew(JcaPGPObjectFactory.class).withAnyArguments().thenReturn(objectFactory);
        assertThatThrownBy(() -> target.verify(signedDataFilePath, downloadedSignature))
                .isInstanceOf(DeployActionException.class);
    }
}
