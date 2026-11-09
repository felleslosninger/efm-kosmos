package no.difi.move.kosmos.service.codesigner;

import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.config.VerificationProperties;
import no.difi.move.kosmos.service.codesigner.PgpTestKeys.TestKeyRing;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.openpgp.PGPSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static no.difi.move.kosmos.service.codesigner.PgpTestKeys.keyRing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Runs {@link GpgServiceImpl} together with the real {@link PublicKeyVerifierImpl}, against key rings and
 * signatures generated for each case.
 */
public class GpgVerificationIT {

    private static final Instant NOW = Instant.parse("2026-11-06T12:00:00Z");

    @TempDir
    Path tempDir;

    private final VerificationProperties verificationProperties = new VerificationProperties();
    private GpgServiceImpl target;
    private Path jar;

    @BeforeEach
    public void setUp() throws Exception {
        KosmosProperties properties = mock(KosmosProperties.class);
        when(properties.getVerification()).thenReturn(verificationProperties);
        target = new GpgServiceImpl(properties, new PublicKeyVerifierImpl(Clock.fixed(NOW, ZoneOffset.UTC)));
        jar = Files.writeString(tempDir.resolve("integrasjonspunkt.jar"), "not really a jar");
    }

    @Test
    public void verify_Sha256SignatureFromValidKey_ShouldBeAccepted() throws Exception {
        TestKeyRing keys = trust(keyRing().build());
        assertTrue(target.verify(jar.toString(), keys.signWithPrimary(jar, HashAlgorithmTags.SHA256)));
    }

    @Test
    public void verify_Sha512SignatureFromSigningSubkey_ShouldBeAccepted() throws Exception {
        TestKeyRing keys = trust(keyRing().primaryFlags(KeyFlags.CERTIFY_OTHER).withSubkey(KeyFlags.SIGN_DATA).build());
        assertTrue(target.verify(jar.toString(), keys.signWithSubkey(jar, HashAlgorithmTags.SHA512)));
    }

    @Test
    public void verify_SignatureOverOtherData_ShouldReturnFalse() throws Exception {
        TestKeyRing keys = trust(keyRing().build());
        String signature = keys.signWithPrimary(jar, HashAlgorithmTags.SHA256);
        Files.writeString(jar, "tampered");
        assertFalse(target.verify(jar.toString(), signature));
    }

    @Test
    public void verify_SignatureFromRevokedKey_ShouldBeRejected() throws Exception {
        TestKeyRing keys = trust(keyRing().revokePrimary().build());
        assertRejected(keys.signWithPrimary(jar, HashAlgorithmTags.SHA256), "revoked");
    }

    @Test
    public void verify_SignatureFromRevokedSubkey_ShouldBeRejected() throws Exception {
        TestKeyRing keys = trust(keyRing().withSubkey(KeyFlags.SIGN_DATA).revokeSubkey().build());
        assertRejected(keys.signWithSubkey(jar, HashAlgorithmTags.SHA256), "revoked");
    }

    @Test
    public void verify_Sha1Signature_ShouldBeRejected() throws Exception {
        TestKeyRing keys = trust(keyRing().build());
        assertRejected(keys.signWithPrimary(jar, HashAlgorithmTags.SHA1), "hash algorithm SHA1");
    }

    @Test
    public void verify_Md5Signature_ShouldBeRejected() throws Exception {
        TestKeyRing keys = trust(keyRing().build());
        assertRejected(keys.signWithPrimary(jar, HashAlgorithmTags.MD5), "hash algorithm MD5");
    }

    @Test
    public void verify_SignatureFromSubkeyWithoutSigningFlag_ShouldBeRejected() throws Exception {
        TestKeyRing keys = trust(keyRing().withSubkey(KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE).build());
        assertRejected(keys.signWithSubkey(jar, HashAlgorithmTags.SHA256), "not allowed to sign");
    }

    @Test
    public void verify_TextDocumentSignature_ShouldBeRejected() throws Exception {
        TestKeyRing keys = trust(keyRing().build());
        String signature = keys.sign(keys.primary(), jar, HashAlgorithmTags.SHA256, PGPSignature.CANONICAL_TEXT_DOCUMENT);
        assertRejected(signature, "signature type");
    }

    @Test
    public void verify_SignatureFileWithoutSignaturePacket_ShouldBeRejected() throws Exception {
        TestKeyRing keys = trust(keyRing().build());
        assertRejected(keys.armoredPublicKey(), "Expected a detached GPG signature");
    }

    private TestKeyRing trust(TestKeyRing keys) throws Exception {
        verificationProperties.setPublicKeyPaths(List.of(keys.writeArmoredPublicKey(tempDir.resolve("key.asc"))));
        return keys;
    }

    private void assertRejected(String signature, String reason) {
        KosmosActionException e = assertThrows(KosmosActionException.class,
                () -> target.verify(jar.toString(), signature));
        assertThat(e.getMessage()).contains(reason);
    }
}
