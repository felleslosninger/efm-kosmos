package no.difi.move.kosmos.service.codesigner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.repo.JavaArchiveRepository;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Verifies JAR files in the home folder before they are used.
 * <p>
 * The home folder may be writable by others than the service account, so neither the JAR files nor
 * the allowlist files there can be trusted on their own.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JarVerifier {

    private static final String DIGEST_ALGORITHM = "SHA-256";

    private final JavaArchiveRepository javaArchiveRepository;
    private final GpgService gpgService;

    /**
     * Verifies the JAR file against the signature published for the version.
     * <p>
     * The signature is downloaded every time instead of being kept in the home folder, so that it cannot
     * be replaced along with the JAR file. Since the signature belongs to the version, a JAR file signed
     * for another version is not accepted either.
     *
     * @return the SHA-256 digest of the verified content, so that later changes to the file can be detected
     * @throws KosmosActionException if the JAR file could not be verified
     */
    public byte[] verify(String version, File jarFile) {
        log.info("Verifying {} against the signature for version {}", jarFile, version);
        boolean verified;
        MessageDigest digest;
        try {
            String signature = javaArchiveRepository.downloadSignature(version);
            digest = newDigest();
            // the digest is calculated from the bytes the signature was checked against, so a file
            // replaced during verification cannot slip through a later comparison
            try (InputStream in = new DigestInputStream(new BufferedInputStream(Files.newInputStream(jarFile.toPath())), digest)) {
                verified = gpgService.verify(in, signature);
            }
        } catch (Exception e) {
            throw new KosmosActionException("Could not verify %s for version %s".formatted(jarFile, version), e);
        }
        if (!verified) {
            throw new KosmosActionException("Invalid signature for %s, version %s".formatted(jarFile, version));
        }
        log.info("{} has been verified", jarFile);
        return digest.digest();
    }

    /**
     * Like {@link #verify(String, File)}, but logs the reason and returns false if the JAR file could not be verified.
     */
    public boolean isVerified(String version, File jarFile) {
        try {
            verify(version, jarFile);
            return true;
        } catch (KosmosActionException e) {
            log.warn("{} is not used: {}", jarFile, describe(e));
            return false;
        }
    }

    /**
     * Checks that the JAR file still has the content that was verified.
     */
    public boolean isUnchanged(File jarFile, byte[] verifiedDigest) {
        MessageDigest digest = newDigest();
        try (InputStream in = new DigestInputStream(new BufferedInputStream(Files.newInputStream(jarFile.toPath())), digest)) {
            in.transferTo(OutputStream.nullOutputStream());
        } catch (IOException e) {
            log.warn("Could not read {}", jarFile, e);
            return false;
        }
        return MessageDigest.isEqual(digest.digest(), verifiedDigest);
    }

    /**
     * The message of the exception, followed by the messages of its causes.
     */
    public static String describe(Throwable throwable) {
        StringBuilder description = new StringBuilder(String.valueOf(throwable.getMessage()));
        for (Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause()) {
            description.append(": ").append(cause.getMessage());
        }
        return description.toString();
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // every Java platform is required to support SHA-256
            throw new IllegalStateException(e);
        }
    }
}
