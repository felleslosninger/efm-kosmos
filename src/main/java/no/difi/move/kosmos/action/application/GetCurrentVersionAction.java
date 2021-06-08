package no.difi.move.kosmos.action.application;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.domain.VersionInfo;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryRepo;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.util.KosmosUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetCurrentVersionAction implements ApplicationAction {

    private final KosmosDirectoryRepo directoryRepo;
    private final ActuatorService actuatorService;
    private final KosmosDirectoryRepo kosmosDirectoryRepo;

    @SneakyThrows
    @Override
    public Application apply(Application application) {
        log.info("Getting current version");
        log.trace("Calling GetCurrentVersionAction.apply() on application {}", application);
        VersionInfo versionInfo = actuatorService.getVersionInfo();
        log.debug("Version info received: {}", versionInfo);
        String version = versionInfo.getVersion();
        String currentVersion = kosmosDirectoryRepo.getAllowlistVersion();
        if (null != version) {
            log.info("The client currently runs integrasjonspunkt version {}", version);
            setCurrentVersion(application, version);
        }
        else if (currentVersion != null) {
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
                        .setFile(directoryRepo.getFile(version, KosmosUtils.DOWNLOAD_JAR_FILE_NAME))
        );
    }
}
