package no.difi.move.kosmos.service.codesigner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import org.bouncycastle.bcpg.SignatureSubpacketTags;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicKeyVerifierImpl implements PublicKeyVerifier {

    private static final Set<Integer> PRIMARY_KEY_SELF_SIGNATURE_TYPES = Set.of(
            PGPSignature.DEFAULT_CERTIFICATION,
            PGPSignature.NO_CERTIFICATION,
            PGPSignature.CASUAL_CERTIFICATION,
            PGPSignature.POSITIVE_CERTIFICATION,
            PGPSignature.DIRECT_KEY);

    private final Clock clock;

    @Override
    public void verify(PGPPublicKeyRing keyRing, PGPPublicKey signerKey) throws KosmosActionException {
        PGPPublicKey primaryKey = keyRing.getPublicKey();
        boolean signerIsPrimary = primaryKey.getKeyID() == signerKey.getKeyID();
        log.info("Verifying public key with fingerprint {}", Hex.toHexString(signerKey.getFingerprint()));

        assertNotRevoked(primaryKey, "Signer public key is revoked");
        assertNotExpired(primaryKey, "Signer public key is expired");
        if (!signerIsPrimary) {
            assertNotRevoked(signerKey, "Signer subkey is revoked");
            assertNotExpired(signerKey, "Signer subkey is expired");
        }
        assertCanSignData(primaryKey, signerKey, signerIsPrimary);
    }

    private static void assertNotRevoked(PGPPublicKey key, String message) {
        // The key file is pinned locally and trusted, so any revocation found there is respected
        // without checking who issued it or why.
        if (key.hasRevocation()) {
            throw new KosmosActionException(message);
        }
    }

    private void assertNotExpired(PGPPublicKey key, String message) {
        long validityInSeconds = key.getValidSeconds();
        log.debug("Key {} validity is set to {} seconds", key.getKeyID(), validityInSeconds);
        if (0 == validityInSeconds) {
            log.debug("Key has no expiry time");
            return;
        }
        Instant creationTime = key.getCreationTime().toInstant();
        Instant expiryTime = creationTime.plusSeconds(validityInSeconds);
        log.debug("Key was created {}, and has expiry time {}", creationTime, expiryTime);
        if (clock.instant().isAfter(expiryTime)) {
            throw new KosmosActionException(message);
        }
    }

    /**
     * The key flags come from the newest self-signature on the key: a user ID certification or
     * direct-key signature for a primary key, a subkey binding signature for a subkey. If the newest
     * self-signature has no key flags, the key is not accepted, even if an older one allowed signing.
     */
    private static void assertCanSignData(PGPPublicKey primaryKey, PGPPublicKey signerKey, boolean signerIsPrimary) {
        Optional<Integer> keyFlags = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(signerKey.getSignatures(), 0), false)
                .filter(sig -> sig.getKeyID() == primaryKey.getKeyID())
                .filter(sig -> signerIsPrimary
                        ? PRIMARY_KEY_SELF_SIGNATURE_TYPES.contains(sig.getSignatureType())
                        : sig.getSignatureType() == PGPSignature.SUBKEY_BINDING)
                .max(Comparator.comparing(PGPSignature::getCreationTime))
                .filter(sig -> sig.hasSubpackets()
                        && sig.getHashedSubPackets().hasSubpacket(SignatureSubpacketTags.KEY_FLAGS))
                .map(sig -> sig.getHashedSubPackets().getKeyFlags());
        log.debug("Key {} has key flags {}", signerKey.getKeyID(), keyFlags.orElse(null));
        if (keyFlags.isEmpty() || (keyFlags.get() & KeyFlags.SIGN_DATA) == 0) {
            throw new KosmosActionException("Signer public key is not allowed to sign data");
        }
    }
}
