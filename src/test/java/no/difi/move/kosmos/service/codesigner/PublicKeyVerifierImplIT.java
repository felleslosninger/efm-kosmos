package no.difi.move.kosmos.service.codesigner;

import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.service.codesigner.PgpTestKeys.TestKeyRing;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static no.difi.move.kosmos.service.codesigner.PgpTestKeys.keyRing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PublicKeyVerifierImplIT {

    // There have been trouble with this test as a regular unit test running
    // in parallel / concurrent with other tests with surefire (got what was
    // probably classpath and/or security provider "pollution").
    // Instead of creating multiple surefire targets, with unique settings
    // around forking, I classified it as an IT test to be run by "failsafe".

    private static final Instant NOW = Instant.parse("2026-11-06T12:00:00Z");
    private static final long ONE_DAY = Duration.ofDays(1).toSeconds();
    private static final long TWENTY_YEARS = Duration.ofDays(20 * 365).toSeconds();

    private final PublicKeyVerifierImpl target = new PublicKeyVerifierImpl(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    public void verify_KeyHasNoExpiry_ShouldPass() {
        TestKeyRing keys = keyRing().build();
        assertDoesNotThrow(() -> target.verify(keys.publicKeyRing(), keys.primaryPublicKey()));
    }

    @Test
    public void verify_KeyIsNotExpired_ShouldPass() {
        TestKeyRing keys = keyRing().primaryValidSeconds(TWENTY_YEARS).build();
        assertDoesNotThrow(() -> target.verify(keys.publicKeyRing(), keys.primaryPublicKey()));
    }

    @Test
    public void verify_KeyIsExpired_ShouldThrow() {
        TestKeyRing keys = keyRing().primaryValidSeconds(ONE_DAY).build();
        assertRejected(keys.publicKeyRing(), keys, false, "expired");
    }

    @Test
    public void verify_PrimaryKeyIsRevoked_ShouldThrow() {
        TestKeyRing keys = keyRing().revokePrimary().build();
        assertRejected(keys.publicKeyRing(), keys, false, "revoked");
    }

    @Test
    public void verify_PrimaryKeyHasNoSigningFlag_ShouldThrow() {
        TestKeyRing keys = keyRing().primaryFlags(KeyFlags.CERTIFY_OTHER).build();
        assertRejected(keys.publicKeyRing(), keys, false, "not allowed to sign");
    }

    @Test
    public void verify_PrimaryKeyHasNoKeyFlags_ShouldThrow() {
        TestKeyRing keys = keyRing().primaryFlags(null).build();
        assertRejected(keys.publicKeyRing(), keys, false, "not allowed to sign");
    }

    @Test
    public void verify_SigningSubkey_ShouldPass() {
        TestKeyRing keys = keyRing().primaryFlags(KeyFlags.CERTIFY_OTHER).withSubkey(KeyFlags.SIGN_DATA).build();
        assertDoesNotThrow(() -> target.verify(keys.publicKeyRing(), keys.subkeyPublicKey()));
    }

    @Test
    public void verify_SubkeyHasNoSigningFlag_ShouldThrow() {
        TestKeyRing keys = keyRing()
                .withSubkey(KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE)
                .build();
        assertRejected(keys.publicKeyRing(), keys, true, "not allowed to sign");
    }

    @Test
    public void verify_SubkeyHasNoKeyFlags_ShouldThrow() {
        TestKeyRing keys = keyRing().withSubkey(null).build();
        assertRejected(keys.publicKeyRing(), keys, true, "not allowed to sign");
    }

    @Test
    public void verify_SubkeyIsRevoked_ShouldThrow() {
        TestKeyRing keys = keyRing().withSubkey(KeyFlags.SIGN_DATA).revokeSubkey().build();
        assertRejected(keys.publicKeyRing(), keys, true, "revoked");
    }

    @Test
    public void verify_SubkeyOfRevokedPrimaryKey_ShouldThrow() {
        TestKeyRing keys = keyRing().withSubkey(KeyFlags.SIGN_DATA).revokePrimary().build();
        assertRejected(keys.publicKeyRing(), keys, true, "revoked");
    }

    @Test
    public void verify_SubkeyIsExpired_ShouldThrow() {
        TestKeyRing keys = keyRing().withSubkey(KeyFlags.SIGN_DATA).subkeyValidSeconds(ONE_DAY).build();
        assertRejected(keys.publicKeyRing(), keys, true, "expired");
    }

    @Test
    public void verify_SubkeyOfExpiredPrimaryKey_ShouldThrow() {
        TestKeyRing keys = keyRing().primaryValidSeconds(ONE_DAY).withSubkey(KeyFlags.SIGN_DATA).build();
        assertRejected(keys.publicKeyRing(), keys, true, "expired");
    }

    @Test
    public void verify_DigdirCodeSigningKey_ShouldPass() throws IOException {
        // The key that signs the integrasjonspunkt releases, from felleslosninger/docs
        PGPPublicKeyRing digdirKey;
        try (InputStream in = PGPUtil.getDecoderStream(new ClassPathResource("/gpg/eformidling-key.asc").getInputStream())) {
            digdirKey = new PGPPublicKeyRing(in, new JcaKeyFingerprintCalculator());
        }
        assertThat(Long.toHexString(digdirKey.getPublicKey().getKeyID())).isEqualToIgnoringCase("CA5643393753ECE3");
        assertDoesNotThrow(() -> target.verify(digdirKey, digdirKey.getPublicKey()));
    }

    private void assertRejected(PGPPublicKeyRing ring, TestKeyRing keys, boolean signWithSubkey, String reason) {
        KosmosActionException e = assertThrows(KosmosActionException.class,
                () -> target.verify(ring, signWithSubkey ? keys.subkeyPublicKey() : keys.primaryPublicKey()));
        assertThat(e.getMessage()).contains(reason);
    }
}
