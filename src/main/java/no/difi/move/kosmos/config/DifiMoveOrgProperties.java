package no.difi.move.kosmos.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@Data
@PropertySource("classpath:integrasjonspunkt-local.properties")
@ConfigurationProperties(prefix = "difi.move.org")
public class DifiMoveOrgProperties {
    private String number;
}
