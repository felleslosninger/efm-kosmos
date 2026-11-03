package no.difi.move.kosmos.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.repo.JavaArchiveRepository;
import no.difi.move.kosmos.repo.KosmosDirectoryJavaArchiveRepository;
import no.difi.move.kosmos.util.KosmosUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrepareApplicationAction implements ApplicationAction {
    private final KosmosProperties properties;
    private final JavaArchiveRepository javaArchiveRepository;
    private final KosmosDirectoryJavaArchiveRepository kosmosDirectoryJavaArchiveRepository;

    public Application apply(Application application) {
        log.info("Preparing application");
        log.trace("Calling PrepareApplicationAction.apply() on application {}", application);
        File downloadJarFile = kosmosDirectoryJavaArchiveRepository.getFile(application.getLatest().getVersion(), KosmosUtils.DOWNLOAD_JAR_FILE_NAME);
        log.debug("The latest version is in file {}", downloadJarFile);
        checkBlocklist(downloadJarFile);

        if (downloadJarFile.exists()) {
            application.getLatest().setFile(downloadJarFile);
            return application;
        }

        log.info("Latest version is different from current, and will be downloaded");
        File unvalidatedJarFile = kosmosDirectoryJavaArchiveRepository.getFile(application.getLatest().getVersion(), KosmosUtils.UNVALIDATED_JAR_FILE_NAME);
        try {
            doDownload(application, unvalidatedJarFile);
        } catch (Exception ex) {
            FileUtils.deleteQuietly(unvalidatedJarFile);
            throw new KosmosActionException("Error occurred when downloading latest version", ex);
        }
        application.setMarkedForValidation(true);
        application.getLatest().setFile(unvalidatedJarFile);
        return application;
    }

    private void checkBlocklist(File downloadFile) {
        boolean blocklistEnabled = properties.getBlocklist().isEnabled();
        if (!blocklistEnabled) {
            log.info("Blocklist functionality is disabled");
        }
        if (blocklistEnabled && kosmosDirectoryJavaArchiveRepository.isBlockListed(downloadFile)) {
            throw new KosmosActionException(
                "The latest version is block listed! Remove %s to allow version.".formatted(
                    kosmosDirectoryJavaArchiveRepository.getBlocklistPath(downloadFile).getAbsolutePath()));
        }
    }

    private void doDownload(Application application, File destination) {
        javaArchiveRepository.downloadJAR(application.getLatest().getVersion(), destination.toPath());
    }
}
