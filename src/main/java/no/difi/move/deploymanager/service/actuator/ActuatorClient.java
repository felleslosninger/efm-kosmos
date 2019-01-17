package no.difi.move.deploymanager.service.actuator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.service.actuator.dto.HealthResource;
import no.difi.move.deploymanager.service.actuator.dto.ShutdownResource;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Component
@Slf4j
public class ActuatorClient {

    private final DeployManagerProperties deployManagerProperties;
    private final RestTemplate restTemplate;

    public ActuatorClient(DeployManagerProperties deployManagerProperties, RestTemplateBuilder restTemplateBuilder) {
        this.deployManagerProperties = deployManagerProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(deployManagerProperties.getActuatorConnectTimeoutInMs())
                .setReadTimeout(deployManagerProperties.getActuatorReadTimeoutInMs())
                .build();
    }

    @SneakyThrows(URISyntaxException.class)
    HealthStatus getStatus() {
        try {
            URI url = deployManagerProperties.getHealthURL().toURI();
            HealthResource healthResource = restTemplate.getForObject(url, HealthResource.class);
            return HealthStatus.fromString(healthResource.getStatus());
        } catch (HttpStatusCodeException e) {
            log.warn("Could not request health status: {} {}", e.getStatusCode(), e.getStatusText());
        } catch (ResourceAccessException e) {
            log.info("Could not request health status: {}", e.getLocalizedMessage());
        }

        return HealthStatus.UNKOWN;
    }

    @SneakyThrows(URISyntaxException.class)
    boolean requestShutdown() {
        try {
            URI url = deployManagerProperties.getShutdownURL().toURI();
            ResponseEntity<ShutdownResource> response = restTemplate.postForEntity(url, null, ShutdownResource.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpStatusCodeException e) {
            log.warn("Could not request shutdown: {} {}", e.getStatusCode(), e.getStatusText());
        } catch (ResourceAccessException e) {
            log.info("Could not request shutdown: {}", e.getLocalizedMessage());
        }

        return false;
    }
}
