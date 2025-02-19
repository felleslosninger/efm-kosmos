package no.difi.move.kosmos.action.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.service.config.RefreshService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class LatestVersionAction implements ApplicationAction {

    private final KosmosProperties properties;
    private final RefreshService refreshService;

    record VersionConfig(Environments integrasjonspunkt) {}
    record Environments(Map<String, Versions> environments) {}
    record Versions(String latest, String earlybird) {}

    static Versions fetchLatestVersions(String environement) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var input = LatestVersionAction.class.getResourceAsStream("/versions/latest-versions.yml");
        var config = mapper.readValue(input, VersionConfig.class);
        var versions = config.integrasjonspunkt.environments.get(environement);
        System.out.println(versions);
        return versions;
    }

    @Override
    public Application apply(Application application) {
        log.info("Getting latest version");
        log.trace("Calling LatestVersionAction.apply() on application: {}", application);
        try {
            refreshService.refreshConfig();
            String latestVersion = getEarlyBirdOption()
                    .orElseGet(() -> properties.getIntegrasjonspunkt().getLatestVersion());
            log.info("The latest version is {}", latestVersion);
            application.setLatest(new ApplicationMetadata().setVersion(latestVersion));
            return application;
        } catch (Exception ex) {
            throw new KosmosActionException("Error getting latest version", ex);
        }
    }

    private Optional<String> getEarlyBirdOption() {
        IntegrasjonspunktProperties integrasjonspunktProperties = properties.getIntegrasjonspunkt();
        String earlyBirdVersion = null;
        if (integrasjonspunktProperties.isEarlyBird()) {
            log.info("The early bird setting is activated");
            earlyBirdVersion = integrasjonspunktProperties.getEarlyBirdVersion();
            log.debug("Early bird version is {}", earlyBirdVersion);
            if (Strings.isNullOrEmpty(earlyBirdVersion)){
                log.info("Early bird setting is activated but no version is selected, will default to latest version");
            }
        }
        return Optional.ofNullable(earlyBirdVersion);
    }
}
