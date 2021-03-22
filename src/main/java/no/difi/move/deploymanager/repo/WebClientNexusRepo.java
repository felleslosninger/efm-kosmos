package no.difi.move.deploymanager.repo;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.google.common.base.Strings;
import io.netty.channel.ChannelOption;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class WebClientNexusRepo implements NexusRepo {

    private final DeployManagerProperties properties;
    private WebClient webClient;

    public WebClientNexusRepo(DeployManagerProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .tcpConfiguration(client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getNexusConnectTimeoutInMs()))
                        .responseTimeout(Duration.ofMillis(properties.getNexusReadTimeoutInMs()))))
                .build();
    }

    @Override
    public void downloadJAR(String version, Path destination) {
        log.trace("Entering WebClientNexusRepo.downloadJAR() with arguments: version: {}, path: {}", version, destination);
        if (Strings.isNullOrEmpty(version)) {
            throw new DeployActionException("Empty version selected for download");
        }
        Objects.requireNonNull(destination, "Download destination missing");
        log.info("Downloading file");
        URI downloadUri = getDownloadURI(version, null);
        log.debug("Downloading file from {}", downloadUri);
        try {
            Flux<DataBuffer> dataBufferFlux = webClient.get().uri(downloadUri)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve().bodyToFlux(DataBuffer.class);
            DataBufferUtils.write(dataBufferFlux, destination, StandardOpenOption.CREATE).block();
            log.debug("File downloaded to {}", destination);
        } catch (WebClientResponseException e) {
            throw new DeployActionException("File download failed", e);
        }
    }

    @Override
    public byte[] getChecksum(String version, String classifier) {
        log.trace("Entering WebClientNexusRepo.getChecksum() with args: version: {}, classifier: {}", version, classifier);
        URI uri = getDownloadURI(version, classifier);
        log.trace("Fetching checksum from URL {}", uri);
        try {
            Mono<String> mono = webClient.get().uri(uri)
                    .retrieve().bodyToMono(String.class);
            String hexString = Optional.ofNullable(mono.block())
                    .orElseThrow(() -> new DeployActionException(String.format("Couldn't download %s", uri)));
            return ByteArrayUtil.hexStringToByteArray(hexString);
        } catch (WebClientResponseException e) {
            throw new DeployActionException("Checksum fetch failed", e);
        }
    }

    @SneakyThrows(URISyntaxException.class)
    private URI getDownloadURI(String version, String classifier) {
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

    public String downloadPublicKey() {
        URI uri = UriComponentsBuilder.fromUriString(properties.getVerification().getPublicKeyURL()).build().toUri();
        log.trace("Downloading public key from {}", uri);
        try {
            Mono<String> mono = webClient.get().uri(uri)
                    .retrieve().bodyToMono(String.class);
            return Optional.ofNullable(mono.block())
                    .orElseThrow(() -> new DeployActionException(String.format("Couldn't download %s", uri)));
        } catch (WebClientResponseException e) {
            throw new DeployActionException("Public key fetch failed", e);
        }
    }

    public String downloadSignature(String version) {
        String classifier = "jar.asc";
        log.trace("Calling NexusRepo.getChecksum() with args: version: {}, classifier: {}", version, classifier);
        URI uri = getDownloadURI(version, classifier);
        log.trace("Downloading signature from {} ", uri);
        try {
            Mono<String> mono = webClient.get().uri(uri)
                    .retrieve().bodyToMono(String.class);
            return Optional.ofNullable(mono.block())
                    .orElseThrow(() -> new DeployActionException(String.format("Couldn't download %s", uri)));
        } catch (WebClientResponseException e) {
            throw new DeployActionException("Signature fetch failed", e);
        }
    }
}
