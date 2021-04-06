package no.difi.move.deploymanager.service.codesigner;

import no.difi.move.deploymanager.action.DeployActionException;
import org.bouncycastle.openpgp.PGPPublicKey;

public interface PublicKeyVerifier {
    void verify(PGPPublicKey publicKey) throws DeployActionException;
}
