package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.repo.NexusRepo;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrepareApplicationAction implements ApplicationAction {

    private final NexusRepo nexusRepo;
    private final DeployDirectoryRepo deployDirectoryRepo;

    public Application apply(Application application) {
        log.debug("Running PrepareApplicationAction.");
        log.info("Preparing application.");
        File downloadFile = deployDirectoryRepo.getFile(application.getLatest().getVersion());

        if (deployDirectoryRepo.isBlackListed(downloadFile)) {
            throw new DeployActionException(
                    String.format("The latest version is black listed! Remove %s to white list.",
                            deployDirectoryRepo.getBlackListedFile(downloadFile).getAbsolutePath()));
        }

        if (!downloadFile.exists()) {
            log.info("Latest is different from current. Downloading newest version.");
            try {
                doDownload(application, downloadFile);
            } catch (Exception ex) {
                throw new DeployActionException("Error getting latest version", ex);
            }
        }

        application.getLatest().setFile(downloadFile);
        return application;
    }

    private void doDownload(Application application, File destination) {
        nexusRepo.downloadJAR(application.getLatest().getVersion(), destination.toPath());
    }

}
