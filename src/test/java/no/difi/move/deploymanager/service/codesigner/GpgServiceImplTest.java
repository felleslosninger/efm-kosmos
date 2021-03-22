package no.difi.move.deploymanager.service.codesigner;

import no.difi.move.deploymanager.action.DeployActionException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class GpgServiceImplTest {

    private final GpgServiceImpl target = new GpgServiceImpl();

    private static String signedDataFilePath;
    private static String downloadedSignature;
    private static String anotherSignature;
    private static String downloadedPublicKey;
    private static String anotherPublicKey;

    @BeforeClass
    public static void beforeClass() throws IOException {
        signedDataFilePath = new ClassPathResource("/gpg/gpgTest.txt").getFile().getAbsolutePath();
        downloadedSignature = new String(readAllBytes(new ClassPathResource("/gpg/signature.asc").getFile().toPath()));
        downloadedPublicKey = new String(readAllBytes(new ClassPathResource("/gpg/public-key.asc").getFile().toPath()));
        anotherSignature = new String(readAllBytes(new ClassPathResource("/gpg/gpgTestOtherSignature.txt.asc").getFile().toPath()));
        anotherPublicKey = new String(readAllBytes(new ClassPathResource("/gpg/invalidPublicKeyEfmTest.asc").getFile().toPath()));
    }

    @Test
    public void verify_Success_ShouldVerifyAndReturnTrue() {
        assertTrue(target.verify(signedDataFilePath, downloadedSignature, downloadedPublicKey));
    }

    @Test
    public void verify_WrongPublicKeyInput_ShouldThrow() {
        assertThatThrownBy(() -> target.verify(signedDataFilePath, downloadedSignature, anotherPublicKey))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void verify_WrongSignatureInput_ShouldReturnFalse() {
        assertThatThrownBy(() -> target.verify(signedDataFilePath, anotherSignature, downloadedPublicKey))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void verify_InputIsNull_ShouldThrow() {
        assertThatThrownBy(() -> target.verify(null, downloadedSignature, downloadedPublicKey))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
