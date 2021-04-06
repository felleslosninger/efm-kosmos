package no.difi.move.deploymanager.service.codesigner;

import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class PublicKeyVerifierImpl implements PublicKeyVerifier {

    @Override
    public void verify(PGPPublicKey publicKey) throws DeployActionException {
        log.info("Verifying public key with fingerprint {}", publicKey.getFingerprint());
        long validityInSeconds = publicKey.getValidSeconds();
        log.debug("Key validity is set to {} seconds", validityInSeconds);
        if (0 == validityInSeconds) {
            log.debug("Key has no expiry time");
            return;
        }
        Instant creationTime = publicKey.getCreationTime().toInstant();
        Instant expiryTime = creationTime.plusSeconds(validityInSeconds);
        log.debug("Key was created {}, and has expiry time {}", creationTime, expiryTime);
        if (Instant.now().isAfter(expiryTime)) {
            throw new DeployActionException("Signer public key is expired");
        }
    }
}
