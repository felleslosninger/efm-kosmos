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
        checkBlocklist(downloadJarFile);

        if (!downloadJarFile.exists()) {
            log.info("Latest version is different from current, and will be downloaded");
            try {
                doDownload(application, downloadJarFile);
                application.setMarkedForValidation(true);
            } catch (Exception ex) {
                throw new DeployActionException("Error occurred when downloading latest version", ex);
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
        if (blocklistEnabled && deployDirectoryRepo.isBlockListed(downloadFile)) {
            throw new DeployActionException(
                    String.format("The latest version is block listed! Remove %s to allow version.",
                            deployDirectoryRepo.getBlocklistPath(downloadFile).getAbsolutePath()));
        }
    }

    private void doDownload(Application application, File destination) {
        nexusRepo.downloadJAR(application.getLatest().getVersion(), destination.toPath());
    }
}
