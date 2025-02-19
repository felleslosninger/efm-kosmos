package no.difi.move.kosmos.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnMissingBean(RefreshServiceImpl.class)
public class NoOpRefreshServiceImpl implements RefreshService {

    @Override
    public void refreshConfig() {
        log.warn("Refreshing latest versjons of Integrasjonspunktet is disabled ('kosmos.integrasjonspunkt.autoRefresh=false')");
    }

}
