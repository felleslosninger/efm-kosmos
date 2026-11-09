package no.difi.move.kosmos.service.codesigner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import org.bouncycastle.bcpg.HashAlgorithmTags;
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
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class GpgServiceImpl implements GpgService {

    /**
     * SHA-1, MD5, RIPEMD-160 and SHA-224 are not accepted. Integrasjonspunkt releases are signed with SHA-256.
     */
    private static final Set<Integer> ACCEPTED_HASH_ALGORITHMS = Set.of(
            HashAlgorithmTags.SHA256,
            HashAlgorithmTags.SHA384,
            HashAlgorithmTags.SHA512,
            HashAlgorithmTags.SHA3_256,
            HashAlgorithmTags.SHA3_512);

    private final KosmosProperties properties;
    private final PublicKeyVerifier keyVerifier;

    @Override
    public boolean verify(String signedData, String downloadedSignature) {
        if (isNullOrEmpty(signedData) || isNullOrEmpty(downloadedSignature)) {
            throw new IllegalArgumentException("One or multiple values are null. " +
                    "\nSignedDataFilePath: " + signedData +
                    "\nSignature: " + downloadedSignature);
        }
        try (InputStream signedDataStream = new BufferedInputStream(new FileInputStream(signedData))) {
            return verify(signedDataStream, downloadedSignature);
        } catch (IOException e) {
            log.error("Could not read the signed data", e);
        }
        return false;
    }

    @Override
    public boolean verify(InputStream signedData, String downloadedSignature) {
        if (signedData == null || isNullOrEmpty(downloadedSignature)) {
            throw new IllegalArgumentException("One or multiple values are null. " +
                    "\nSignedData: " + signedData +
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
        assertSignatureIsAcceptable(signature);
        PGPPublicKeyRing signerKeyRing = publicKeyPaths.stream()
                .map(this::readPublicKey)
                .filter(Objects::nonNull)
                .map(file -> getSignerKeyRing(signature, file))
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new KosmosActionException("Signer public key not found in keyring"));
        PGPPublicKey signerKey = signerKeyRing.getPublicKey(signature.getKeyID());
        keyVerifier.verify(signerKeyRing, signerKey);
        return doVerify(signedData, signature, signerKey);
    }

    private static void assertSignatureIsAcceptable(PGPSignature signature) {
        if (signature.getSignatureType() != PGPSignature.BINARY_DOCUMENT) {
            throw new KosmosActionException("Unexpected GPG signature type %d, expected a binary document signature"
                    .formatted(signature.getSignatureType()));
        }
        if (!ACCEPTED_HASH_ALGORITHMS.contains(signature.getHashAlgorithm())) {
            throw new KosmosActionException("GPG signature uses hash algorithm %s, which is not accepted"
                    .formatted(hashAlgorithmName(signature.getHashAlgorithm())));
        }
    }

    private static String hashAlgorithmName(int hashAlgorithm) {
        try {
            return PGPUtil.getDigestName(hashAlgorithm);
        } catch (PGPException e) {
            return String.valueOf(hashAlgorithm);
        }
    }

    private boolean doVerify(InputStream signedData, PGPSignature signature, PGPPublicKey publicKey) {
        log.debug("Attempting GPG verification with public key {}", publicKey.getKeyID());
        try {
            signature.init(new JcaPGPContentVerifierBuilderProvider(), publicKey);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = signedData.read(buffer)) != -1) {
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

    private PGPPublicKeyRing getSignerKeyRing(PGPSignature signature, PGPPublicKeyRingCollection file) {
        log.info("Looking for signer key");
        final long keyID = signature.getKeyID();
        log.trace("Looking for signer key {} in file {}", keyID, file);
        try {
            return file.getPublicKeyRing(keyID);
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
            Object pgpObject = decoder.nextObject();
            if (pgpObject instanceof PGPSignatureList pgpSignatures && !pgpSignatures.isEmpty()) {
                return pgpSignatures.get(0);
            }
            throw new KosmosActionException("Expected a detached GPG signature, but found %s"
                    .formatted(pgpObject == null ? "nothing" : pgpObject.getClass().getSimpleName()));
        } catch (IOException e) {
            log.warn("Could not read signature from {}", signature);
        }
        return null;
    }

}
