package no.difi.move.deploymanager.service.codesigner;

import no.difi.move.deploymanager.action.DeployActionException;
import org.assertj.core.util.Lists;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    private final GpgServiceImpl target = new GpgServiceImpl();

    private static String signedDataFilePath;
    private static String downloadedSignature;
    private static String anotherSignature;
    private static List<String> downloadedPublicKeys;
    private static List<String> noMatchingPublicKeys;
    private static String matchingPublicKey;
    private static String notMatchingPublicKey;

    @BeforeClass
    public static void beforeClass() throws IOException {
        signedDataFilePath = new ClassPathResource("/gpg/gpgTest.txt").getFile().getAbsolutePath();
        downloadedSignature = new String(readAllBytes(new ClassPathResource("/gpg/signature.asc").getFile().toPath()));
        matchingPublicKey = new String(readAllBytes(new ClassPathResource("/gpg/public-key.asc").getFile().toPath()));
        notMatchingPublicKey = new String(readAllBytes(new ClassPathResource("/gpg/invalidPublicKeyEfmTest.asc").getFile().toPath()));
        anotherSignature = new String(readAllBytes(new ClassPathResource("/gpg/gpgTestOtherSignature.txt.asc").getFile().toPath()));
    }

    @Before
    public void setUp() {
        downloadedPublicKeys = Collections.singletonList(matchingPublicKey);
        noMatchingPublicKeys = Collections.singletonList(notMatchingPublicKey);
    }

    @Test
    public void verify_Success_ShouldVerifyAndReturnTrue() {
        List<String> bothMatchingAndNotMatchingKeys = Lists.newArrayList(matchingPublicKey, notMatchingPublicKey);
        assertTrue(target.verify(signedDataFilePath, downloadedSignature, bothMatchingAndNotMatchingKeys));
    }

    @Test
    public void verify_WrongPublicKeyInput_ShouldThrow() {
        assertThatThrownBy(() -> target.verify(signedDataFilePath, downloadedSignature, noMatchingPublicKeys))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void verify_WrongSignatureInput_ShouldThrow() {
        assertThatThrownBy(() -> target.verify(signedDataFilePath, anotherSignature, downloadedPublicKeys))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void verify_InputIsNull_ShouldThrow() {
        assertThatThrownBy(() -> target.verify(null, downloadedSignature, downloadedPublicKeys))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void verify_NoSignature_ShouldThrow() throws Exception {
        JcaPGPObjectFactory objectFactory = mock(JcaPGPObjectFactory.class);
        when(objectFactory.nextObject()).thenReturn(null);
        whenNew(JcaPGPObjectFactory.class).withAnyArguments().thenReturn(objectFactory);
        assertThatThrownBy(() -> target.verify(signedDataFilePath, downloadedSignature, downloadedPublicKeys))
                .isInstanceOf(DeployActionException.class);
    }
}
