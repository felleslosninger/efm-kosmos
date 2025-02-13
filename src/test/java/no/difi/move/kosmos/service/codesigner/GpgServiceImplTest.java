package no.difi.move.kosmos.service.codesigner;

import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.config.VerificationProperties;
import org.assertj.core.util.Lists;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled("Caused by: java.lang.SecurityException: digest missing for org/bouncycastle/openpgp/PGPPublicKey.class")
@ExtendWith(MockitoExtension.class)
public class GpgServiceImplTest {

    @Mock
    private KosmosProperties properties;
    @Mock
    private VerificationProperties verificationProperties;
    @Mock
    private PublicKeyVerifier keyVerifier;

    @InjectMocks
    private GpgServiceImpl target;

    private static String signedDataFilePath;
    private static String downloadedSignature;
    private static String anotherSignature;
    private static List<Resource> downloadedPublicKeys;
    private static List<Resource> noMatchingPublicKeys;
    private static Resource matchingPublicKeyFilePath;
    private static Resource notMatchingPublicKeyFilePath;

    @BeforeAll
    public static void beforeClass() throws IOException {
        signedDataFilePath = new ClassPathResource("/gpg/gpgTest.txt").getFile().getAbsolutePath();
        downloadedSignature = new String(readAllBytes(new ClassPathResource("/gpg/signature.asc").getFile().toPath()));
        anotherSignature = new String(readAllBytes(new ClassPathResource("/gpg/gpgTestOtherSignature.txt.asc").getFile().toPath()));
        matchingPublicKeyFilePath = new ClassPathResource("/gpg/public-key.asc");
        notMatchingPublicKeyFilePath = new ClassPathResource("/gpg/invalidPublicKeyEfmTest.asc");
    }

    @BeforeEach
    public void setUp() {
        when(properties.getVerification()).thenReturn(verificationProperties);
        downloadedPublicKeys = Collections.singletonList(matchingPublicKeyFilePath);
        noMatchingPublicKeys = Collections.singletonList(notMatchingPublicKeyFilePath);
    }

    @Test
    public void verify_Success_ShouldVerifyAndReturnTrue() {
        doNothing().when(keyVerifier).verify(any(PGPPublicKey.class));
        List<Resource> bothMatchingAndNotMatchingKeys = Lists.newArrayList(matchingPublicKeyFilePath, notMatchingPublicKeyFilePath);
        when(verificationProperties.getPublicKeyPaths()).thenReturn(bothMatchingAndNotMatchingKeys);
        assertTrue(target.verify(signedDataFilePath, downloadedSignature));
    }

    @Test
    public void verify_WrongPublicKeyInput_ShouldThrow() {
        when(verificationProperties.getPublicKeyPaths()).thenReturn(noMatchingPublicKeys);
        assertThrows(KosmosActionException.class, () -> target.verify(signedDataFilePath, downloadedSignature));
    }

    @Test
    public void verify_WrongSignatureInput_ShouldThrow() {
        when(verificationProperties.getPublicKeyPaths()).thenReturn(downloadedPublicKeys);
        assertThrows(KosmosActionException.class, () -> target.verify(signedDataFilePath, anotherSignature));
    }

    @Test
    public void verify_NoSignature_ShouldThrow() {
        when(verificationProperties.getPublicKeyPaths()).thenReturn(downloadedPublicKeys);

        try (MockedConstruction<JcaPGPObjectFactory> mockObjectFactory = mockConstruction(JcaPGPObjectFactory.class,
                (mock, context) -> when(mock.nextObject()).thenReturn(null))) {
            assertThrows(KosmosActionException.class, () -> target.verify(signedDataFilePath, downloadedSignature));
        }
    }

    @Test
    public void verify_ExpiredPublicKey_ShouldThrow() {
        when(verificationProperties.getPublicKeyPaths()).thenReturn(downloadedPublicKeys);
        doThrow(new KosmosActionException("Expired key")).when(keyVerifier).verify(any(PGPPublicKey.class));
        assertThrows(KosmosActionException.class, () -> target.verify(signedDataFilePath, downloadedSignature));
    }

}
