package no.difi.move.kosmos.service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(value = "kosmos.integrasjonspunkt.autoRefresh", havingValue = "true")
public class RefreshServiceImpl implements RefreshService {

    // FIXME denne må erstattes med noe som frisker opp siste versjoner fra github filen
    // https://raw.githubusercontent.com/felleslosninger/efm-integrasjonspunkt/refs/heads/feature-MOVE-3684-integrasjonspunkt-v3/latest-versions.yml

    @Override
    public void refreshConfig() {
        log.info("Refreshing latest versjons of Integrasjonspunktet");
    }

}