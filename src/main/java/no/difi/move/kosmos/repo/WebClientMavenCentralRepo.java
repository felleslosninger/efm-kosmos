package no.difi.move.kosmos.repo;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import com.google.common.base.Strings;
import io.netty.channel.ChannelOption;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
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
public class WebClientMavenCentralRepo implements MavenCentralRepo {

    private final KosmosProperties properties;
    private final WebClient webClient;

    public WebClientMavenCentralRepo(KosmosProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .tcpConfiguration(client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getMavenCentralConnectTimeoutInMs()))
                        .responseTimeout(Duration.ofMillis(properties.getMavenCentralReadTimeoutInMs()))))
                .build();
    }

    @Override
    public void downloadJAR(String version, Path destination) {
        log.trace("Entering WebClientMavenCentralRepo.downloadJAR() with arguments: version: {}, path: {}", version, destination);
        if (Strings.isNullOrEmpty(version)) {
            throw new KosmosActionException("Empty version selected for download");
        }
        Objects.requireNonNull(destination, "Download destination missing");
        log.info("Downloading file");
        URI downloadUri = getDownloadURI(version, null);
        log.debug("Downloading file from {}", downloadUri);
        try {
            Flux<DataBuffer> dataBufferFlux = webClient.get().uri(downloadUri)
                    .retrieve().bodyToFlux(DataBuffer.class);
            DataBufferUtils.write(dataBufferFlux, destination, StandardOpenOption.CREATE).block();
            log.debug("File downloaded to {}", destination);
        } catch (WebClientResponseException e) {
            throw new KosmosActionException("File download failed", e);
        }
    }

    @Override
    public byte[] getChecksum(String version, String classifier) {
        log.trace("Entering WebClientMavenCentralRepo.getChecksum() with args: version: {}, classifier: {}", version, classifier);
        URI uri = getDownloadURI(version, classifier);
        log.trace("Fetching checksum from URL {}", uri);
        try {
            Mono<String> mono = webClient.get().uri(uri)
                    .retrieve().bodyToMono(String.class);
            String hexString = Optional.ofNullable(mono.block())
                    .orElseThrow(() -> new KosmosActionException("Couldn't download %s".formatted(uri)));
            return ByteArrayUtil.hexStringToByteArray(hexString);
        } catch (WebClientResponseException e) {
            throw new KosmosActionException("Checksum fetch failed", e);
        }
    }

    @SneakyThrows(URISyntaxException.class)
    private URI getDownloadURI(String version, String classifier) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(properties.getMavenCentral().toURI())
                .path(properties.getGroupId() + properties.getArtifactId() + version + "/" + "integrasjonspunkt-" + version + ".jar");


        if (classifier != null) {
            builder.path(classifier);
        }
        log.trace("Built Maven central download URI: {}", builder.build().toUri());
        return builder.build().toUri();
    }

    @Override
    public String downloadSignature(String version) {
        String classifier = ".asc";
        log.trace("Calling MavenCentralRepo.getChecksum() with args: version: {}, classifier: {}", version, classifier);
        URI uri = getDownloadURI(version, classifier);
        log.trace("Downloading signature from {} ", uri);
        try {
            Mono<String> mono = webClient.get().uri(uri)
                    .retrieve().bodyToMono(String.class);
            return Optional.ofNullable(mono.block())
                    .orElseThrow(() -> new KosmosActionException("Couldn't download %s".formatted(uri)));
        } catch (WebClientResponseException e) {
            throw new KosmosActionException("Signature fetch failed", e);
        }
    }
}
