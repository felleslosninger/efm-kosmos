package no.difi.move.kosmos.service.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(value = "kosmos.integrasjonspunkt.autoRefresh", havingValue = "true")
public class RefreshServiceImpl implements RefreshService {

    @Value("${kosmos.integrasjonspunkt.versionsURL}")
    private String url;

    private VersionsConfig lastValidConfig;

    @Override
    public VersionsConfig refreshConfig() {
        var yaml = RestClient.builder().build().get().uri(url).retrieve().body(String.class);
        var newConfig = parseLatestVersions(yaml);
        if (newConfig != null) {
            log.info("Refreshed versions config");
            lastValidConfig = newConfig;
        }
        return lastValidConfig;
    }

    static VersionsConfig parseLatestVersions(String yamlString) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(yamlString, VersionsConfig.class);
        } catch (Exception ex) {
            log.error("Unable to decode YAML with latest versions", ex);
        }
        return null;
    }

}