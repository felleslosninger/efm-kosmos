package no.difi.move.kosmos.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.repo.KosmosDirectoryRepo;
import no.difi.move.kosmos.repo.MavenCentralRepo;
import no.difi.move.kosmos.util.KosmosUtils;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrepareApplicationAction implements ApplicationAction {
    private final KosmosProperties properties;
    private final MavenCentralRepo mavenCentralRepo;
    private final KosmosDirectoryRepo kosmosDirectoryRepo;

    public Application apply(Application application) {
        log.info("Preparing application");
        log.trace("Calling PrepareApplicationAction.apply() on application {}", application);
        File downloadJarFile = kosmosDirectoryRepo.getFile(application.getLatest().getVersion(), KosmosUtils.DOWNLOAD_JAR_FILE_NAME);
        log.debug("The latest version is in file {}", downloadJarFile);
        checkBlocklist(downloadJarFile);

        if (!downloadJarFile.exists()) {
            log.info("Latest version is different from current, and will be downloaded");
            try {
                doDownload(application, downloadJarFile);
                application.setMarkedForValidation(true);
            } catch (Exception ex) {
                throw new KosmosActionException("Error occurred when downloading latest version", ex);
            }
        }

        application.getLatest().setFile(downloadJarFile);
        return application;
    }

    private void checkBlocklist(File downloadFile) {
        boolean blocklistEnabled = properties.getBlocklist().isEnabled();
        if (!blocklistEnabled) {
            log.info("Blocklist functionality is disabled");
        }
        if (blocklistEnabled && kosmosDirectoryRepo.isBlockListed(downloadFile)) {
            throw new KosmosActionException(
                    "The latest version is block listed! Remove %s to allow version.".formatted(
                            kosmosDirectoryRepo.getBlocklistPath(downloadFile).getAbsolutePath()));
        }
    }

    private void doDownload(Application application, File destination) {
        mavenCentralRepo.downloadJAR(application.getLatest().getVersion(), destination.toPath());
    }
}
