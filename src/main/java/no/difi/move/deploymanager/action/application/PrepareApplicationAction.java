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

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PrepareApplicationAction implements ApplicationAction {

    private final DeployManagerProperties properties;
    private final NexusRepo nexusRepo;
    private final DeployDirectoryRepo deployDirectoryRepo;

    public Application apply(Application application) {
        log.debug("Running PrepareApplicationAction.");
        log.info("Preparing application.");
        File downloadJarFile = getDownloadFile(application, "integrasjonspunkt-%s.jar");
        File downloadAscFile = getDownloadFile(application, "integrasjonspunkt-%s.jar.asc");

        if (deployDirectoryRepo.isBlackListed(downloadJarFile)) {
            throw new DeployActionException(
                    String.format("The latest version is black listed! Remove %s to white list.",
                            deployDirectoryRepo.getBlackListedFile(downloadJarFile).getAbsolutePath()));
        }

        if (!downloadJarFile.exists()) {
            log.info("Latest is different from current. Downloading newest version.");
            try {
                doDownload(application, downloadJarFile);
                doDownload(application, downloadAscFile);
            } catch (Exception ex) {
                throw new DeployActionException("Error getting latest version", ex);
            }
        }

        application.getLatest().setFile(downloadJarFile);
        application.getSignature().setFile(downloadAscFile);
        return application;
    }

    private void doDownload(Application application, File destination) {
        nexusRepo.downloadJAR(application.getLatest().getVersion(), destination.toPath());
    }

    private File getDownloadFile(Application application, String fileName) {
        String home = properties.getIntegrasjonspunkt().getHome();
        String latestVersion = application.getLatest().getVersion();
        return new File(home, String.format(fileName, latestVersion));
    }
}
