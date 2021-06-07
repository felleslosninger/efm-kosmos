package no.difi.move.kosmos;

import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.config.DifiMoveOrgProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({KosmosProperties.class, DifiMoveOrgProperties.class})
public class KosmosMain {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(KosmosMain.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
}
