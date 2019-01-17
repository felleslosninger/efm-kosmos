package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.repo.NexusRepo;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.*;

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

    public Application apply(@NotNull Application application) {
        log.debug("Running PrepareApplicationAction.");
        log.info("Preparing application.");
        File downloadFile = getDownloadFile(application);

        if (deployDirectoryRepo.isBlackListed(downloadFile)) {
            throw new DeployActionException(
                    String.format("The latest version is black listed! Remove %s to white list.",
                            deployDirectoryRepo.getBlackListedFile(downloadFile).getAbsolutePath()));
        }

        if (shouldDownload(application, downloadFile)) {
            log.info("Latest is different from current. Downloading newest version.");
            try {
                doDownload(application, downloadFile);
            } catch (IOException ex) {
                throw new DeployActionException("Error getting latest version", ex);
            }
        }

        application.getLatest().setFile(downloadFile);
        return application;
    }

    private void doDownload(Application application, File destination) throws IOException {
        try (InputStream is = nexusRepo.getArtifact(application.getLatest().getVersion(), null).openStream();
             OutputStream os = new FileOutputStream(destination)) {
            IOUtils.copy(is, os);
        }
    }

    private boolean shouldDownload(Application application, File fileToDownload) {
        return !fileToDownload.exists() || !application.isSameVersion();
    }

    private File getDownloadFile(Application application) {
        String root = properties.getRoot();
        String latestVersion = application.getLatest().getVersion();
        return new File(root, String.format("integrasjonspunkt-%s.jar", latestVersion));
    }
}
