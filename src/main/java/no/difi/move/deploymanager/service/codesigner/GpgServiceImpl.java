package no.difi.move.deploymanager.service.codesigner;

import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
@Slf4j
public class GpgServiceImpl implements GpgService {

    @Override
    public boolean verify(String signedDataFilePath, String downloadedSignature, String downloadedPublicKey) {
        if(isNullOrEmpty(signedDataFilePath) || isNullOrEmpty(downloadedSignature) || isNullOrEmpty(downloadedPublicKey)) {
            throw new IllegalArgumentException("One or multiple values are null. " +
                    "\nSignedDataFilePath: " + signedDataFilePath +
                    "\nSignature: " + downloadedSignature +
                    "\nPublic Key: " +  downloadedPublicKey);
        }
        try (InputStream signedData = new FileInputStream(signedDataFilePath);
             InputStream signature = new ByteArrayInputStream(downloadedSignature.getBytes());
             InputStream publicKey = new ByteArrayInputStream(downloadedPublicKey.getBytes())) {
            log.trace("Attempting GPG verification with signature {} \nand public key {}", downloadedSignature, downloadedPublicKey);

            PGPObjectFactory pgpFactory = new PGPObjectFactory(PGPUtil.getDecoderStream(signature), new JcaKeyFingerprintCalculator());
            PGPSignature sig = ((PGPSignatureList) pgpFactory.nextObject()).get(0);
            PGPPublicKeyRingCollection pgpPubKeyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKey), new JcaKeyFingerprintCalculator());
            PGPPublicKey key = Optional.ofNullable(pgpPubKeyRingCollection.getPublicKey(sig.getKeyID()))
                    .orElseThrow(() -> new DeployActionException("Signer public key not found in keyring"));

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
