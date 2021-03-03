package no.difi.move.deploymanager.action.application;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.IntegrasjonspunktProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.service.config.RefreshService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class LatestVersionAction implements ApplicationAction {

    private final DeployManagerProperties properties;
    private final RefreshService refreshService;

    @Override
    public Application apply(Application application) {
        log.info("Getting latest version");
        log.trace("Calling LatestVersionAction.apply() on application: {}", application);
        try {
            refreshService.refreshConfig();
            Optional<String> earlyBirdVersion = getEarlyBirdOption();
            String latestVersion = earlyBirdVersion.orElseGet(() -> properties.getIntegrasjonspunkt().getLatestVersion());
            log.info("The latest version is {}", latestVersion);
            application.setLatest(new ApplicationMetadata().setVersion(latestVersion));
            return application;
        } catch (Exception ex) {
            throw new DeployActionException("Error getting latest version", ex);
        }
    }

    private Optional<String> getEarlyBirdOption() {
        IntegrasjonspunktProperties integrasjonspunktProperties = properties.getIntegrasjonspunkt();
        boolean earlyBird = integrasjonspunktProperties.isEarlyBird();
        String earlyBirdVersion = null;
        if (earlyBird) {
            log.info("The early bird setting is activated");
            earlyBirdVersion = integrasjonspunktProperties.getEarlyBirdVersion();
            if (Strings.isNullOrEmpty(earlyBirdVersion)) {
                throw new IllegalStateException("Early bird setting is activated, but no version is specified");
            }
            log.debug("Early bird version is {}", earlyBirdVersion);
        }
        return Optional.ofNullable(earlyBirdVersion);
    }
}
