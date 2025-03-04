package no.difi.move.kosmos.action.application;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.service.config.RefreshService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LatestVersionAction implements ApplicationAction {

    private final KosmosProperties properties;
    private final RefreshService refreshService;

    @Override
    public Application apply(Application application) {
        log.info("Getting latest version");
        log.trace("Calling LatestVersionAction.apply() on application: {}", application);
        try {
            var profile = properties.getIntegrasjonspunkt().getProfile();
            var useEarlybird = properties.getIntegrasjonspunkt().isEarlyBird();
            var config = refreshService.refreshConfig().integrasjonspunkt().environments().get(profile);
            if (config == null) throw new IllegalStateException("Environment config for profile '" + profile + "' not found");
            var version = useEarlybird ? config.earlybird() : config.latest();
            if (useEarlybird) {
                log.info("The early bird setting is activated");
                log.debug("Early bird version is {}", version);
            }
            if (Strings.isNullOrEmpty(version)) {
                log.info("Early bird setting is activated but no version is selected, will default to latest version");
                version = config.latest();
            }
            log.info("The latest version is {}", version);
            application.setLatest(new ApplicationMetadata().setVersion(version));
            return application;
        } catch (Exception ex) {
            throw new KosmosActionException("Error getting latest version", ex);
        }
    }

}
