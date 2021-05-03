package no.difi.move.deploymanager.action.application;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerifyVersionComplianceAction implements ApplicationAction {

    private final DeployManagerProperties properties;

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
            throw new DeployActionException("No version to verify");
        }
        log.info("Latest version is {}", latestVersion);
        return compareVersions(application, supportedMajorVersion, latestVersion);
    }

    private Application compareVersions(Application application, String supportedMajorVersion, String latestVersion) {
        log.trace("Comparing supported major ({}) and latest available ({}) versions", supportedMajorVersion, latestVersion);
        int latestMajor = resolveMajorVersionFromSemanticVersion(latestVersion);
        int currentlySupported = resolveMajorVersionFromSemanticVersion(supportedMajorVersion);
        if (latestMajor > currentlySupported) {
            throw new DeployActionException(
                    String.format("Latest version (%s) is not supported yet. The currently supported major version is %s.",
                            latestMajor, currentlySupported));
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
            throw new DeployActionException("Invalid version specifier encountered", e);
        }
    }
}
