package no.difi.move.deploymanager.action.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.repo.NexusRepo;
import no.difi.move.deploymanager.service.jarsigner.JarsSignerService;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@Slf4j
@RequiredArgsConstructor
@Validated
public class ValidateAction implements ApplicationAction {

    private final NexusRepo nexusRepo;
    private final JarsSignerService jarsSignerService;

    @Override
    public Application apply(Application application) {
        log.info("Validating jar");
        log.trace("Calling ValidateAction.apply on application {}", application);
        try {
            assertChecksumIsCorrect(application, ALGORITHM.SHA1);
            assertChecksumIsCorrect(application, ALGORITHM.MD5);
            jarsSignerService.verify(application.getLatest().getFile().getAbsolutePath());
            return application;
        } catch (Exception ex) {
            throw new DeployActionException("Error validating jar", ex);
        }
    }

    private void assertChecksumIsCorrect(Application application, ALGORITHM algorithm) throws IOException, NoSuchAlgorithmException {
        log.trace("Calling ValidateAction.assertChecksumIsCorrect() with args: application: {}, algorithm: {}", application, algorithm);
        byte[] hashFromRepo = getHashFromRepo(application.getLatest().getVersion(), algorithm);
        log.trace("Hash from repo: {}", hashFromRepo);
        byte[] fileHash = getFileHash(application.getLatest().getFile(), algorithm);
        log.trace("File hash: {}", fileHash);
        if (!MessageDigest.isEqual(fileHash, hashFromRepo)) {
            throw new DeployActionException(String.format("%s verification failed", algorithm.getName()));
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private byte[] getFileHash(File file, ALGORITHM algorithm) throws IOException, NoSuchAlgorithmException {
        byte[] buffer = new byte[8192];
        MessageDigest instance = MessageDigest.getInstance(algorithm.getName());
        try (DigestInputStream digestInputStream = new DigestInputStream(new FileInputStream(file), instance)) {
            while (digestInputStream.read(buffer) != -1) ;
            return instance.digest();
        }
    }

    private byte[] getHashFromRepo(String applicationVersion, ALGORITHM algorithm) {
        return nexusRepo.getChecksum(applicationVersion, "jar." + algorithm.getFileNameSuffix());
    }

    @RequiredArgsConstructor
    @Getter
    private enum ALGORITHM {
        MD5("MD5", "md5"),
        SHA1("SHA-1", "sha1");

        private final String name;
        private final String fileNameSuffix;
    }
}
