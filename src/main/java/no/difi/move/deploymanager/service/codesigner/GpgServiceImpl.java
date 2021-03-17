package no.difi.move.deploymanager.service.codesigner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class GpgServiceImpl implements GpgService {

    @Override
    public boolean verify(String signedDataFilePath, String signatureFilePath, String publicKeyFilePath) {
        try (InputStream signedData = new FileInputStream(signedDataFilePath);
             InputStream signature = new ByteArrayInputStream(signatureFilePath.getBytes());
             InputStream publicKey = new ByteArrayInputStream(publicKeyFilePath.getBytes())) {
            log.trace("Attempting GPG verification with signature {} \nand public key {}", signatureFilePath, publicKeyFilePath);

            PGPObjectFactory pgpFactory = new PGPObjectFactory(PGPUtil.getDecoderStream(signature), new JcaKeyFingerprintCalculator());
            PGPSignature sig = ((PGPSignatureList) pgpFactory.nextObject()).get(0);
            PGPPublicKeyRingCollection pgpPubKeyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKey), new JcaKeyFingerprintCalculator());
            PGPPublicKey key = pgpPubKeyRingCollection.getPublicKey(sig.getKeyID());

            sig.init(new BcPGPContentVerifierBuilderProvider(), key);
            byte[] buffer = new byte[1024];
            int read = 0;

            while ((read = signedData.read(buffer)) != -1) {
                sig.update(buffer, 0, read);
            }
            return sig.verify();

        } catch (IOException e) {
            log.error("IOException occured, could not verify GPG signature ", e);
        } catch (PGPException e) {
            log.error("PGPException occured, could not verify GPG signature", e);
        }

        return false;
    }
}
