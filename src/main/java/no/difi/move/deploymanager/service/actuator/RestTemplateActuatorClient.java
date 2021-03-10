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
public class RestTemplateActuatorClient implements ActuatorClient {

    private final DeployManagerProperties deployManagerProperties;
    @Getter
    private final RestTemplate restTemplate;

    public RestTemplateActuatorClient(DeployManagerProperties deployManagerProperties, RestTemplateBuilder restTemplateBuilder) {
        this.deployManagerProperties = deployManagerProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(deployManagerProperties.getActuatorConnectTimeoutInMs()))
                .setReadTimeout(Duration.ofMillis(deployManagerProperties.getActuatorReadTimeoutInMs()))
                .build();
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public HealthStatus getStatus() {
        try {
            URI url = deployManagerProperties.getIntegrasjonspunkt().getHealthURL().toURI();
            log.trace("Fetching health status from URL: {}", url);
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

    @Override
    @SneakyThrows(URISyntaxException.class)
    public boolean requestShutdown() {
        log.info("Requesting shutdown");
        try {
            URI url = deployManagerProperties.getIntegrasjonspunkt().getShutdownURL().toURI();
            log.trace("Requesting shutdown at URL: {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ShutdownResource> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, ShutdownResource.class);
            HttpStatus responseStatus = response.getStatusCode();
            log.debug("Received response status code: {}", responseStatus);
            return responseStatus == HttpStatus.OK;
        } catch (HttpStatusCodeException e) {
            log.warn("Could not request shutdown: {} {}", e.getStatusCode(), e.getStatusText());
        } catch (ResourceAccessException e) {
            log.info("Could not request shutdown: {}", e.getLocalizedMessage());
        }

        return false;
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public VersionInfo getVersionInfo() {
        log.info("Getting version information");
        try {
            URI infoUri = deployManagerProperties.getIntegrasjonspunkt().getInfoURL().toURI();
            log.trace("Fetching version info from URI {}", infoUri);
            InfoResource resource = restTemplate.getForObject(infoUri, InfoResource.class);
            log.debug("Parsed InfoResource: {}", resource);
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
