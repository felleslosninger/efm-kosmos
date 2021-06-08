package no.difi.move.kosmos.action.application;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryRepo;
import no.difi.move.kosmos.util.KosmosUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerifyVersionComplianceAction implements ApplicationAction {

    private final KosmosProperties properties;
    private final KosmosDirectoryRepo deployDirectoryRepo;

    @Override
    public Application apply(Application application) {
        log.trace("Calling VerifyVersionComplianceAction.apply() on application {}", application);
        String supportedMajorVersion = properties.getIntegrasjonspunkt().getSupportedMajorVersion();
        if (Strings.isNullOrEmpty(supportedMajorVersion)) {
            return application;
        }
        log.info("Currently supported major version is set to {}", supportedMajorVersion);
        String latestVersion = application.getLatest().getVersion();
        if (null == latestVersion) {
            throw new KosmosActionException("No version to verify");
        }
        log.info("Latest version is {}", latestVersion);
        return compareVersions(application, supportedMajorVersion, latestVersion);
    }

    private Application compareVersions(Application application, String supportedMajorVersion, String latestVersion) {
        log.trace("Comparing supported major ({}) and latest available ({}) versions", supportedMajorVersion, latestVersion);
        int latestMajor = resolveMajorVersionFromSemanticVersion(latestVersion);
        int currentlySupported = resolveMajorVersionFromSemanticVersion(supportedMajorVersion);
        if (latestMajor > currentlySupported) {
            log.warn(String.format("Latest version (%s) is not supported yet. The currently supported major version is %s. " +
                            "Attempting to start integrasjonspunkt with previous version based on .allowlisted file if available.",
                    latestMajor, currentlySupported));
            String version = deployDirectoryRepo.getAllowlistVersion();
            if(version != null) {
                application.setLatest(
                        new ApplicationMetadata()
                                .setVersion(version)
                                .setFile(deployDirectoryRepo.getFile(version, KosmosUtils.DOWNLOAD_JAR_FILE_NAME)));

                return application;
            }
            throw new KosmosActionException(
                    String.format("Latest version (%s) is not supported yet. The currently supported major version is %s. " +
                                    "Please change your supported major version(deploymanager.integrasjonspunkt.supported-major-version) to match the latest %s and restart the application.",
                            latestMajor, currentlySupported, latestMajor));
        }
        return application;
    }

    private int resolveMajorVersionFromSemanticVersion(String semanticVersion) {
        if (semanticVersion == null) {
            throw new IllegalArgumentException("Semantic version is null");
        }
        String majorVersionString;
        int firstDot = semanticVersion.indexOf('.');
        if (firstDot == -1) {
            majorVersionString = semanticVersion;
        } else {
            majorVersionString = semanticVersion.substring(0, firstDot);
        }
        try {
            return Integer.parseInt(majorVersionString);
        } catch (NumberFormatException e) {
            throw new KosmosActionException("Invalid version specifier encountered", e);
        }
    }
}
