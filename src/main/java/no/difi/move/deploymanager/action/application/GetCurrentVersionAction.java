package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.VersionInfo;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GetCurrentVersionAction implements ApplicationAction {

    private final DeployManagerProperties properties;
    private final DeployDirectoryRepo directoryRepo;
    private final ActuatorService actuatorService;

    @Override
    public Application apply(Application application) {
        log.debug("Running GetCurrentVersionAction.");
        try {
            log.info("Getting current version");
            VersionInfo versionInfo = actuatorService.getVersionInfo();

            Properties metadata = directoryRepo.getMetadata();
            application.setCurrent(
                    new ApplicationMetadata()
                            .setVersion(versionInfo.getVersion())
                            .setRepositoryId(properties.getRepository())
                            .setSha1(metadata.getProperty("sha1"))
                            .setFile(getFile(metadata.getProperty("filename")))
            );
        } catch (IOException ex) {
            log.error(null, ex);
            throw new DeployActionException("Failed to get current version", ex);
        }
        return application;
    }

    private File getFile(String filename) {
        return filename != null ? directoryRepo.getFile(filename) : null;
    }
}
