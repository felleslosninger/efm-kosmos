package no.difi.move.deploymanager.repo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@RequiredArgsConstructor
public class NexusRepo {

    private final DeployManagerProperties properties;

    @SneakyThrows(URISyntaxException.class)
    public URL getArtifact(@NotNull String version, String classifier) throws MalformedURLException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(properties.getNexus().toURI())
                .pathSegment("service", "local", "artifact", "maven", "content")
                .queryParam("r", properties.getRepository())
                .queryParam("g", properties.getGroupId())
                .queryParam("a", properties.getArtifactId())
                .queryParam("v", version);

        if (classifier != null) {
            builder.queryParam("e", classifier);
        }

        return builder.build().toUri().toURL();
    }
}
