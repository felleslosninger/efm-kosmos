package no.difi.move.deploymanager.service.actuator;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.VersionInfo;
import no.difi.move.deploymanager.service.actuator.dto.HealthResource;
import no.difi.move.deploymanager.service.actuator.dto.InfoResource;
import no.difi.move.deploymanager.service.actuator.dto.ShutdownResource;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class ActuatorClient {

    private final DeployManagerProperties deployManagerProperties;
    @Getter
    private final RestTemplate restTemplate;

    public ActuatorClient(DeployManagerProperties deployManagerProperties, RestTemplateBuilder restTemplateBuilder) {
        this.deployManagerProperties = deployManagerProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(deployManagerProperties.getActuatorConnectTimeoutInMs()))
                .setReadTimeout(Duration.ofMillis(deployManagerProperties.getActuatorReadTimeoutInMs()))
                .build();
    }

    @SneakyThrows(URISyntaxException.class)
    HealthStatus getStatus() {
        try {
            URI url = deployManagerProperties.getIntegrasjonspunkt().getHealthURL().toURI();
            return Optional.ofNullable(restTemplate.getForObject(url, HealthResource.class))
                    .map(p -> HealthStatus.fromString(p.getStatus()))
                    .orElse(HealthStatus.UNKNOWN);
        } catch (HttpStatusCodeException e) {
            log.warn("Could not request health status: {} {}", e.getStatusCode(), e.getStatusText());
        } catch (ResourceAccessException e) {
            log.info("Could not request health status: {}", e.getLocalizedMessage());
        }

        return HealthStatus.UNKNOWN;
    }

    @SneakyThrows(URISyntaxException.class)
    boolean requestShutdown() {
        try {
            URI url = deployManagerProperties.getIntegrasjonspunkt().getShutdownURL().toURI();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ShutdownResource> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, ShutdownResource.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpStatusCodeException e) {
            log.warn("Could not request shutdown: {} {}", e.getStatusCode(), e.getStatusText());
        } catch (ResourceAccessException e) {
            log.info("Could not request shutdown: {}", e.getLocalizedMessage());
        }

        return false;
    }

    @SneakyThrows(URISyntaxException.class)
    VersionInfo getVersionInfo() {
        try {
            URI infoUri = deployManagerProperties.getIntegrasjonspunkt().getInfoURL().toURI();
            InfoResource resource = restTemplate.getForObject(infoUri, InfoResource.class);
            return (Optional.ofNullable(resource)
                    .map(infoResource -> VersionInfo.builder()
                            .resolved(infoResource.getBuild() != null)
                            .version(infoResource.getBuild().getVersion()))
                    .orElseGet(() -> VersionInfo.builder().resolved(false)))
                    .build();
        } catch (HttpStatusCodeException e) {
            log.warn("Could not request info status: {} {}", e.getStatusCode(), e.getStatusText());
        } catch (ResourceAccessException e) {
            log.info("Could not request info status: {}", e.getLocalizedMessage());
        }
        return VersionInfo.builder().resolved(false).build();
    }
}
