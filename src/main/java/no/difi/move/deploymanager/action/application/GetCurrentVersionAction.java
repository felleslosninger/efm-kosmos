package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.domain.VersionInfo;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetCurrentVersionAction implements ApplicationAction {

    private final DeployDirectoryRepo directoryRepo;
    private final ActuatorService actuatorService;

    @Override
    public Application apply(Application application) {
        log.debug("Running GetCurrentVersionAction.");
        log.info("Getting current version");
        VersionInfo versionInfo = actuatorService.getVersionInfo();
        String version = versionInfo.getVersion();
        if (null != version) {
            log.info("The client currently runs integrasjonspunkt version {}", version);
            application.setCurrent(
                    new ApplicationMetadata()
                            .setVersion(version)
                            .setFile(directoryRepo.getFile(version))
            );
        } else {
            log.info("No running integrasjonspunkt version found");
        }

        return application;
    }

}
