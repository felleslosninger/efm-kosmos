package no.difi.move.deploymanager.service.actuator;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.VersionInfo;
import no.difi.move.deploymanager.service.actuator.dto.HealthResource;
import no.difi.move.deploymanager.service.actuator.dto.InfoResource;
import no.difi.move.deploymanager.service.actuator.dto.ShutdownResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class WebClientActuatorClient implements ActuatorClient {

    private final DeployManagerProperties properties;
    private final WebClient webClient;

    public WebClientActuatorClient(DeployManagerProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .tcpConfiguration(client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getActuatorConnectTimeoutInMs()))
                        .responseTimeout(Duration.ofMillis(properties.getActuatorReadTimeoutInMs()))))
                .build();
    }

    @Override
    public HealthStatus getStatus() {
        try {
            URI url = properties.getIntegrasjonspunkt().getHealthURL().toURI();
            log.trace("Fetching health status from URL: {}", url);
            Mono<HealthStatus> healthResourceMono = webClient.get().uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(HealthResource.class)
                    .flatMap(r -> Mono.just(HealthStatus.fromString(r.getStatus())));
            return healthResourceMono.block();
        } catch (WebClientResponseException e) {
            log.debug("Could not obtain health status: {}, {}", e.getStatusCode(), e.getStatusText());
        } catch (URISyntaxException e) {
            log.warn("Could not request health status: {}, {}", e.getMessage(), e.getReason());
        } catch (Exception e) {
            log.warn("Could not request health status: {}", e.getMessage());
        }
        return HealthStatus.UNKNOWN;
    }

    @Override
    public boolean requestShutdown() {
        try {
            log.info("Requesting shutdown");
            URI url = properties.getIntegrasjonspunkt().getShutdownURL().toURI();
            log.trace("Requesting shutdown at URL: {}", url);
            Mono<ResponseEntity<ShutdownResource>> httpStatusMono = webClient.post().uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().toEntity(ShutdownResource.class);
            ResponseEntity<ShutdownResource> response = httpStatusMono.block();
            return response != null && response.getStatusCode().is2xxSuccessful();
        } catch (WebClientResponseException e) {
            log.debug("Could not achieve shutdown: {}, {}", e.getStatusCode(), e.getStatusText());
        } catch (URISyntaxException e) {
            log.warn("Could not request shutdown: {}, {}", e.getMessage(), e.getReason());
        } catch (Exception e) {
            log.warn("Could not request shutdown: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public VersionInfo getVersionInfo() {
        try {
            log.info("Fetching version information");
            URI infoUri = properties.getIntegrasjonspunkt().getInfoURL().toURI();
            log.trace("Fetching version info from URI {}", infoUri);
            Mono<InfoResource> infoResourceMono = webClient.get().uri(infoUri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(InfoResource.class);
            InfoResource resource = infoResourceMono.block();
            return (Optional.ofNullable(resource)
                    .map(infoResource -> VersionInfo.builder()
                            .resolved(infoResource.getBuild() != null)
                            .version(infoResource.getBuild().getVersion()))
                    .orElseGet(() -> VersionInfo.builder().resolved(false)))
                    .build();
        } catch (WebClientResponseException e) {
            log.debug("Could not obtain version information: {}, {}", e.getStatusCode(), e.getStatusText());
        } catch (URISyntaxException e) {
            log.warn("Could not request version information: {} {}", e.getMessage(), e.getReason());
        } catch (Exception e) {
            log.warn("Could not request version information: {}", e.getMessage());
        }
        return VersionInfo.builder().resolved(false).build();
    }

}


