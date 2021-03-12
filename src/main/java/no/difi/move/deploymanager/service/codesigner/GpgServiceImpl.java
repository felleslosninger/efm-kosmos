package no.difi.move.deploymanager.service.codesigner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
@Slf4j
@RequiredArgsConstructor
public class GpgServiceImpl implements GpgService {

    @Override
    public boolean verify(String signedDataFilePath, String signatureFilePath, String publicKeyFilePath) {
        try (InputStream signedData = new FileInputStream(signedDataFilePath);
             InputStream signature = new FileInputStream(signatureFilePath);
             InputStream publicKey = new FileInputStream(publicKeyFilePath)) {

            PGPObjectFactory pgpFactory = new PGPObjectFactory(PGPUtil.getDecoderStream(signature), new JcaKeyFingerprintCalculator());
            PGPSignature sig = ((PGPSignatureList) pgpFactory.nextObject()).get(0);
            PGPPublicKeyRingCollection pgpPubKeyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKey), new JcaKeyFingerprintCalculator());
            PGPPublicKey key = pgpPubKeyRingCollection.getPublicKey(sig.getKeyID());


            //TODO kvifor treng ein denne outputten?
            //TODO fullfør testing av denne klassen og den nye implementasjonen.
            Iterator<String> userIDs = key.getUserIDs();
            while (userIDs.hasNext()) {
                System.out.println(userIDs.next());
            }

            sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key);
            byte[] buffer = new byte[1024];
            int read = 0;

            while ((read = signedData.read(buffer)) != -1) {
                sig.update(buffer, 0, read);
            }

            return sig.verify();

        } catch (IOException e) {
            //TODO betre feilmeldinga og vise til signaturfil.
            log.error("IOException occured, could not verify GPG signature ", e);
        } catch (PGPException e) {
            log.error("PGPException occured, could not verify GPG signature", e);
        }

        return false;
    }
}
