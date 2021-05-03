package no.difi.move.deploymanager;

import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.DifiMoveOrgProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableConfigurationProperties({DeployManagerProperties.class, DifiMoveOrgProperties.class})
public class DeployManagerMain {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DeployManagerMain.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
}
