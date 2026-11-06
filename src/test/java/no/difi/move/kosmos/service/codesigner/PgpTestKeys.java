package no.difi.move.kosmos.service.codesigner;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyPacket;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

/**
 * Builds OpenPGP key rings and detached signatures for tests, so that each case (revoked key, missing
 * signing flag, weak hash and so on) can be set up without keeping private keys in the repository.
 */
final class PgpTestKeys {

    static final int CERTIFY_AND_SIGN = KeyFlags.CERTIFY_OTHER | KeyFlags.SIGN_DATA;

    // RSA key generation is slow, so all test key rings share the same two key pairs.
    private static final KeyPair PRIMARY_KEY_MATERIAL = generateRsaKeyPair();
    private static final KeyPair SUBKEY_MATERIAL = generateRsaKeyPair();

    private PgpTestKeys() {
    }

    static Builder keyRing() {
        return new Builder();
    }

    static final class Builder {
        private Instant createdAt = Instant.parse("2020-01-01T00:00:00Z");
        private Integer primaryFlags = CERTIFY_AND_SIGN;
        private long primaryValidSeconds = 0;
        private boolean subkey = false;
        private Integer subkeyFlags = KeyFlags.SIGN_DATA;
        private long subkeyValidSeconds = 0;
        private boolean revokePrimary = false;
        private boolean revokeSubkey = false;

        Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @param flags the primary key's key flags, or null to leave out the key flags subpacket
         */
        Builder primaryFlags(Integer flags) {
            this.primaryFlags = flags;
            return this;
        }

        Builder primaryValidSeconds(long seconds) {
            this.primaryValidSeconds = seconds;
            return this;
        }

        /**
         * @param flags the subkey's key flags, or null to leave out the key flags subpacket
         */
        Builder withSubkey(Integer flags) {
            this.subkey = true;
            this.subkeyFlags = flags;
            return this;
        }

        Builder subkeyValidSeconds(long seconds) {
            this.subkeyValidSeconds = seconds;
            return this;
        }

        Builder revokePrimary() {
            this.revokePrimary = true;
            return this;
        }

        Builder revokeSubkey() {
            this.revokeSubkey = true;
            return this;
        }

        TestKeyRing build() {
            try {
                Date created = Date.from(createdAt);
                PGPKeyPair primary = new JcaPGPKeyPair(PublicKeyPacket.VERSION_4,
                        PublicKeyAlgorithmTags.RSA_GENERAL, PRIMARY_KEY_MATERIAL, created);
                PGPDigestCalculator sha1 = new JcaPGPDigestCalculatorProviderBuilder().build()
                        .get(HashAlgorithmTags.SHA1);
                PGPKeyRingGenerator generator = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION,
                        primary, "KOSMOS test <kosmos-test@example.com>", sha1,
                        selfSignatureSubpackets(primaryFlags, primaryValidSeconds), null,
                        signerBuilder(HashAlgorithmTags.SHA256), null);
                PGPKeyPair sub = null;
                if (subkey) {
                    sub = new JcaPGPKeyPair(PublicKeyPacket.VERSION_4,
                            PublicKeyAlgorithmTags.RSA_GENERAL, SUBKEY_MATERIAL, created);
                    generator.addSubKey(sub, selfSignatureSubpackets(subkeyFlags, subkeyValidSeconds), null);
                }
                PGPPublicKeyRing ring = generator.generatePublicKeyRing();
                if (revokePrimary) {
                    ring = revoke(ring, primary, ring.getPublicKey(), PGPSignature.KEY_REVOCATION);
                }
                if (revokeSubkey) {
                    ring = revoke(ring, primary, ring.getPublicKey(sub.getKeyID()), PGPSignature.SUBKEY_REVOCATION);
                }
                // Round trip, so the ring looks exactly like one read from a key file
                ring = new PGPPublicKeyRing(ring.getEncoded(), new JcaKeyFingerprintCalculator());
                return new TestKeyRing(ring, primary, sub);
            } catch (PGPException | IOException e) {
                throw new IllegalStateException("Could not build test key ring", e);
            }
        }

        private static PGPSignatureSubpacketVector selfSignatureSubpackets(Integer flags, long validSeconds) {
            PGPSignatureSubpacketGenerator subpackets = new PGPSignatureSubpacketGenerator();
            if (flags != null) {
                subpackets.setKeyFlags(false, flags);
            }
            if (validSeconds > 0) {
                subpackets.setKeyExpirationTime(false, validSeconds);
            }
            return subpackets.generate();
        }

        private static PGPPublicKeyRing revoke(PGPPublicKeyRing ring, PGPKeyPair primary, PGPPublicKey key,
                                               int revocationType) throws PGPException {
            PGPSignatureGenerator generator = new PGPSignatureGenerator(
                    signerBuilder(HashAlgorithmTags.SHA256), primary.getPublicKey());
            generator.init(revocationType, primary.getPrivateKey());
            PGPSignature revocation = revocationType == PGPSignature.KEY_REVOCATION
                    ? generator.generateCertification(key)
                    : generator.generateCertification(ring.getPublicKey(), key);
            return PGPPublicKeyRing.insertPublicKey(ring, PGPPublicKey.addCertification(key, revocation));
        }
    }

    record TestKeyRing(PGPPublicKeyRing publicKeyRing, PGPKeyPair primary, PGPKeyPair subkey) {

        PGPPublicKey primaryPublicKey() {
            return publicKeyRing.getPublicKey();
        }

        PGPPublicKey subkeyPublicKey() {
            return publicKeyRing.getPublicKey(subkey.getKeyID());
        }

        Resource writeArmoredPublicKey(Path file) throws IOException {
            Files.writeString(file, armoredPublicKey(), StandardCharsets.US_ASCII);
            return new FileSystemResource(file);
        }

        String armoredPublicKey() throws IOException {
            return armor(publicKeyRing::encode);
        }

        String signWithPrimary(Path data, int hashAlgorithm) throws IOException, PGPException {
            return sign(primary, data, hashAlgorithm, PGPSignature.BINARY_DOCUMENT);
        }

        String signWithSubkey(Path data, int hashAlgorithm) throws IOException, PGPException {
            return sign(subkey, data, hashAlgorithm, PGPSignature.BINARY_DOCUMENT);
        }

        String sign(PGPKeyPair signer, Path data, int hashAlgorithm, int signatureType)
                throws IOException, PGPException {
            PGPSignatureGenerator generator = new PGPSignatureGenerator(
                    signerBuilder(hashAlgorithm), signer.getPublicKey());
            generator.init(signatureType, signer.getPrivateKey());
            generator.update(Files.readAllBytes(data));
            PGPSignature signature = generator.generate();
            return armor(signature::encode);
        }
    }

    private interface Encoder {
        void encode(OutputStream out) throws IOException;
    }

    private static String armor(Encoder encoder) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ArmoredOutputStream armored = new ArmoredOutputStream(bytes)) {
            encoder.encode(armored);
        }
        return bytes.toString(StandardCharsets.US_ASCII);
    }

    private static JcaPGPContentSignerBuilder signerBuilder(int hashAlgorithm) {
        return new JcaPGPContentSignerBuilder(PublicKeyAlgorithmTags.RSA_GENERAL, hashAlgorithm);
    }

    private static KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
