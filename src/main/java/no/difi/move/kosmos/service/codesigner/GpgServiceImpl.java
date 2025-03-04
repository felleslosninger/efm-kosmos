package no.difi.move.kosmos.service.codesigner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class GpgServiceImpl implements GpgService {

    private final KosmosProperties properties;
    private final PublicKeyVerifier keyVerifier;

    @Override
    public boolean verify(String signedData, String downloadedSignature) {
        if (isNullOrEmpty(signedData) || isNullOrEmpty(downloadedSignature)) {
            throw new IllegalArgumentException("One or multiple values are null. " +
                    "\nSignedDataFilePath: " + signedData +
                    "\nSignature: " + downloadedSignature);
        }
        final List<Resource> publicKeyPaths = properties.getVerification().getPublicKeyPaths();
        if (publicKeyPaths.isEmpty()) {
            throw new IllegalArgumentException("Cannot verify signature due to missing keys");
        }
        log.info("Verifying signed data");
        PGPSignature signature = Optional.ofNullable(readSignature(downloadedSignature))
                .orElseThrow(() -> new KosmosActionException(
                "Unable to read GPG signature from %s".formatted(downloadedSignature)));
        PGPPublicKey signerKey = publicKeyPaths.stream()
                .map(this::readPublicKey)
                .filter(Objects::nonNull)
                .map(file -> getSignerKey(signature, file))
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new KosmosActionException("Signer public key not found in keyring"));
        keyVerifier.verify(signerKey);
        return doVerify(signedData, signature, signerKey);
    }

    private boolean doVerify(String signedData, PGPSignature signature, PGPPublicKey publicKey) {
        log.debug("Attempting GPG verification with public key {}", publicKey.getKeyID());
        try (InputStream signedDataStream = new BufferedInputStream(new FileInputStream(signedData))) {
            signature.init(new JcaPGPContentVerifierBuilderProvider(), publicKey);
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = signedDataStream.read(buffer)) != -1) {
                signature.update(buffer, 0, read);
            }
            return signature.verify();
        } catch (IOException e) {
            log.error("Could not read the signed data", e);
        } catch (PGPException e) {
            log.error("Could not verify GPG signature", e);
        }
        log.debug("Verification failed for key {}", publicKey.getKeyID());
        return false;
    }

    private PGPPublicKey getSignerKey(PGPSignature signature, PGPPublicKeyRingCollection file) {
        log.info("Looking for signer key");
        final long keyID = signature.getKeyID();
        log.trace("Looking for signer key {} in file {}", keyID, file);
        try {
            return file.getPublicKey(keyID);
        } catch (Exception e) {
            log.warn("Could not get signer public key from file {}", file);
        }
        return null;
    }

    private PGPPublicKeyRingCollection readPublicKey(Resource path) {
        log.info("Reads public key from {}", path);
        try (InputStream keyStream = PGPUtil.getDecoderStream(new FileInputStream(path.getFile()))) {
            return new JcaPGPPublicKeyRingCollection(keyStream);
        } catch (IOException e) {
            log.warn("Could not read public key from {}", path, e);
        } catch (PGPException e) {
            log.warn("Invalid public key encountered in {}", path, e);
        }
        return null;
    }

    private PGPSignature readSignature(String signature) {
        log.info("Reading PGP signature");
        try (InputStream signatureStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(signature.getBytes()))) {
            JcaPGPObjectFactory decoder = new JcaPGPObjectFactory(signatureStream);
            PGPSignatureList pgpSignatures = Optional.ofNullable((PGPSignatureList) decoder.nextObject())
                    .orElseThrow(() -> new KosmosActionException("Unable to read signature"));
            return pgpSignatures.get(0);
        } catch (IOException e) {
            log.warn("Could not read signature from {}", signature);
        }
        return null;
    }

}