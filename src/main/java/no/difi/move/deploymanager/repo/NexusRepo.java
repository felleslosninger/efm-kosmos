package no.difi.move.deploymanager.repo;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.repo.dto.ApplicationMetadataResource;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class NexusRepo {

    private final DeployManagerProperties properties;
    @Getter
    private final RestTemplate restTemplate;

    public NexusRepo(DeployManagerProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(properties.getNexusConnectTimeoutInMs()))
                .setReadTimeout(Duration.ofMillis(properties.getNexusReadTimeoutInMs()))
                .build();
    }

    public ApplicationMetadataResource getApplicationMetadata() {
        return ApplicationMetadataResource.builder()
                .baseVersion(properties.getIntegrasjonspunkt().getLatestVersion())
                .sha1(properties.getIntegrasjonspunkt().getLatestChecksum())
                .build();
    }

    public void downloadJAR(@NotNull String version, @NotNull Path destination) {
        // Optional Accept header
        RequestCallback requestCallback = request -> request.getHeaders()
                .setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

        // Streams the response instead of loading it all in memory
        ResponseExtractor<Void> responseExtractor = response -> {
            Files.copy(response.getBody(), destination);
            return null;
        };

        restTemplate.execute(getURI(version, null), HttpMethod.GET, requestCallback, responseExtractor);
    }

    public byte[] getChecksum(@NotNull String version, String classifier) {
        URI uri = getURI(version, classifier);
        String hash = Optional.ofNullable(restTemplate.getForObject(uri, String.class))
                .orElseThrow(() -> new DeployActionException(String.format("Couldn't download %s", uri)));
        return ByteArrayUtil.hexStringToByteArray(hash);
    }

    @SneakyThrows(URISyntaxException.class)
    private URI getURI(@NotNull String version, String classifier) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(properties.getNexus().toURI())
                .pathSegment("service", "local", "artifact", "maven", "content")
                .queryParam("r", properties.getRepository())
                .queryParam("g", properties.getGroupId())
                .queryParam("a", properties.getArtifactId())
                .queryParam("v", version);

        if (classifier != null) {
            builder.queryParam("e", classifier);
        }

        return builder.build().toUri();
    }
}
