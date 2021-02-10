package no.difi.move.deploymanager.action.application;

import com.google.common.base.Strings;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
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
        log.debug("Running VerifyVersionComplianceAction");
        String supportedMajorVersion = properties.getIntegrasjonspunkt().getSupportedMajorVersion();
        if (Strings.isNullOrEmpty(supportedMajorVersion)) {
            return application;
        }
        log.debug("Currently supported major version is set to {}", supportedMajorVersion);
        String latestVersion = application.getLatest().getVersion();
        if (null == latestVersion) {
            throw new DeployActionException("No version to verify");
        }
        log.debug("Latest available version is {}", latestVersion);
        return compareVersions(application, supportedMajorVersion, latestVersion);
    }

    private Application compareVersions(Application application, String supportedMajorVersion, String latestVersion) {
        log.info("Comparing supported major ({}) and latest available ({}) versions", supportedMajorVersion, latestVersion);
        try {
            Semver latestMajor = new Semver(latestVersion, Semver.SemverType.LOOSE);
            Semver currentlySupported = new Semver(supportedMajorVersion, Semver.SemverType.LOOSE);
            if (latestMajor.getMajor() > currentlySupported.getMajor()) {
                throw new DeployActionException(
                        String.format("Latest version (%s) is not supported yet. The currently supported major version is %s.",
                                latestMajor, currentlySupported));
            }
            return application;
        } catch (SemverException e) {
            throw new DeployActionException("Invalid version specifier encountered", e);
        }
    }
}
