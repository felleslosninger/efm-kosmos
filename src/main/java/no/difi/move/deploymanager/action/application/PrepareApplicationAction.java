package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.repo.NexusRepo;
import no.difi.move.deploymanager.util.DeployUtils;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrepareApplicationAction implements ApplicationAction {
    private final DeployManagerProperties properties;
    private final NexusRepo nexusRepo;
    private final DeployDirectoryRepo deployDirectoryRepo;

    public Application apply(Application application) {
        log.info("Preparing application");
        log.trace("Calling PrepareApplicationAction.apply() on application {}", application);
        File downloadJarFile = deployDirectoryRepo.getFile(application.getLatest().getVersion(), DeployUtils.DOWNLOAD_JAR_FILE_NAME);
        log.debug("The latest version is in file {}", downloadJarFile);
        checkBlacklist(downloadJarFile);

        if (!downloadJarFile.exists()) {
            log.info("Latest version is different from current, and will be downloaded");
            try {
                doDownload(application, downloadJarFile);
            } catch (Exception ex) {
                throw new DeployActionException("Error occurred when downloading latest version", ex);
            }
        }

        application.getLatest().setFile(downloadJarFile);
        return application;
    }

    private void checkBlacklist(File downloadFile) {
        boolean blacklistEnabled = properties.getBlacklist().isEnabled();
        log.info("Blacklist functionality is disabled");
        if (blacklistEnabled && deployDirectoryRepo.isBlackListed(downloadFile)) {
            throw new DeployActionException(
                    String.format("The latest version is black listed! Remove %s to white list.",
                            deployDirectoryRepo.getBlacklistPath(downloadFile).getAbsolutePath()));
        }
    }

    private void doDownload(Application application, File destination) {
        nexusRepo.downloadJAR(application.getLatest().getVersion(), destination.toPath());
    }
}
