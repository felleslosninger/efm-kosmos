package no.difi.move.kosmos.service.codesigner;

import no.difi.move.kosmos.action.KosmosActionException;
import org.bouncycastle.openpgp.PGPPublicKey;

public interface PublicKeyVerifier {
    void verify(PGPPublicKey publicKey) throws KosmosActionException;
}
