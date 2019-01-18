package no.difi.move.deploymanager;

import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DeployManagerProperties.class)
public class DeployManagerMain {

    public static void main(String[] args) {
        SpringApplication.run(DeployManagerMain.class, args);
    }
}
