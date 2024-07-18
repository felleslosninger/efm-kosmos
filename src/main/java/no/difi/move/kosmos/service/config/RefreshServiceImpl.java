package no.difi.move.kosmos.service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.refresh.ConfigDataContextRefresher;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(value = "spring.cloud.config.enabled", havingValue = "true")
public class RefreshServiceImpl implements RefreshService {

    private final ConfigDataContextRefresher contextRefresher;

    @Override
    public void refreshConfig() {
        log.info("Refreshing configuration");
        contextRefresher.refresh();
    }
}