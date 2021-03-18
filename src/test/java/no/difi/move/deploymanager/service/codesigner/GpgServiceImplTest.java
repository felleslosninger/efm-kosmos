package no.difi.move.deploymanager.service.codesigner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GpgServiceImplTest {

    @InjectMocks
    private GpgServiceImpl target;

    private final String signedDataFilePath = "c:/path/to/file";
    private final String downloadedSignature = "signature";
    private final String downloadedPublicKey = "public key";

    @Before
    public void setUp() {
        
    }

    @Test
    public void verify_Success_ShouldVerifyAndReturnTrue() {
        //when(target.verify(signedDataFilePath, downloadedSignature, downloadedPublicKey)).thenReturn(true);
        //TODO må sjekke summane og verifisere i denne testen at det som kjem inn er likt det eg forventa.
    }

    @Test
    public void verify_Failed_ShouldThrow() {
        //TODO skriv test
    }
}
