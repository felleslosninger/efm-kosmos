package no.difi.move.kosmos.service.codesigner;

import no.difi.move.kosmos.action.KosmosActionException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

public interface PublicKeyVerifier {

    /**
     * Checks that a key may be used to verify a signature.
     *
     * @param keyRing   the key ring the signer key belongs to
     * @param signerKey the primary key or subkey that made the signature
     * @throws KosmosActionException if the key, or the primary key of its ring, is revoked or expired,
     *                               or if the signer key is not allowed to sign data
     */
    void verify(PGPPublicKeyRing keyRing, PGPPublicKey signerKey) throws KosmosActionException;
}
