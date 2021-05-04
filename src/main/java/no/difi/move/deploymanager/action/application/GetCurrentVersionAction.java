package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.VersionInfo;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import no.difi.move.deploymanager.util.DeployUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetCurrentVersionAction implements ApplicationAction {

    private final DeployDirectoryRepo directoryRepo;
    private final ActuatorService actuatorService;
    private final DeployManagerProperties properties;

    @Override
    public Application apply(Application application) {
        log.info("Getting current version");
        log.trace("Calling GetCurrentVersionAction.apply() on application {}", application);
        VersionInfo versionInfo = actuatorService.getVersionInfo();
        log.debug("Version info received: {}", versionInfo);
        String version = versionInfo.getVersion();
        String currentVersion = properties.getIntegrasjonspunkt().getCurrentVersion();
        if (null != version) {
            log.info("The client currently runs integrasjonspunkt version {}", version);
            setCurrentVersion(application, version);
        } else if (currentVersion != null) {
            log.info("No running integrasjonspunkt found, but starting previously used version {}", currentVersion);
            setCurrentVersion(application, currentVersion);
        }
        else {
            log.info("No running integrasjonspunkt found");
        }

        return application;
    }

    public void setCurrentVersion(Application application, String version) {
        application.setCurrent(
                new ApplicationMetadata()
                        .setVersion(version)
                        .setFile(directoryRepo.getFile(version, DeployUtils.DOWNLOAD_JAR_FILE_NAME))
        );
    }
}
