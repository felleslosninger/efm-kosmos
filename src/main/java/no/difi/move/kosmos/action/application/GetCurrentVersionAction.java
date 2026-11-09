package no.difi.move.kosmos.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.domain.VersionInfo;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryJavaArchiveRepository;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.service.codesigner.JarVerifier;
import no.difi.move.kosmos.util.KosmosUtils;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetCurrentVersionAction implements ApplicationAction {

    private final KosmosDirectoryJavaArchiveRepository directoryRepo;
    private final ActuatorService actuatorService;
    private final JarVerifier jarVerifier;

    @Override
    public Application apply(Application application) {
        log.info("Getting current version");
        log.trace("Calling GetCurrentVersionAction.apply() on application {}", application);
        VersionInfo versionInfo = actuatorService.getVersionInfo();
        log.debug("Version info received: {}", versionInfo);
        String version = versionInfo.getVersion();
        if (null != version) {
            log.info("The client currently runs integrasjonspunkt version {}", version);
            setCurrentVersion(application, version);
            return application;
        }

        String allowlistVersion = directoryRepo.getAllowlistVersion();
        if (allowlistVersion == null) {
            log.info("No running integrasjonspunkt found");
        } else if (!jarVerifier.isVerified(allowlistVersion, getJarFile(allowlistVersion))) {
            // the allowlist file only tells which version was used, anyone who can write to the home folder can create one
            log.warn("No running integrasjonspunkt found, and the previously used version {} could not be verified", allowlistVersion);
        } else {
            log.info("No running integrasjonspunkt found, but starting previously used version {}", allowlistVersion);
            setCurrentVersion(application, allowlistVersion);
        }
        return application;
    }

    private void setCurrentVersion(Application application, String version) {
        application.setCurrent(
            new ApplicationMetadata()
                .setVersion(version)
                .setFile(getJarFile(version))
        );
    }

    private File getJarFile(String version) {
        return directoryRepo.getFile(version, KosmosUtils.DOWNLOAD_JAR_FILE_NAME);
    }
}
