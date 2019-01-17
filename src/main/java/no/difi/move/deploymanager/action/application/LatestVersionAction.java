package no.difi.move.deploymanager.action.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LatestVersionAction implements ApplicationAction {

    private final DeployManagerProperties properties;

    @Override
    public Application apply(@NotNull Application application) {
        log.debug("Running LatestVersionAction.");

        try {
            log.info("Getting latest version");
            URLConnection connection = properties.getNexusProxyURL().openConnection();
            InputStream inputStream = connection.getInputStream();
            String result = IOUtils.toString(inputStream, connection.getContentEncoding());
            ApplicationMetadataDto dto = new ObjectMapper().readValue(result, ApplicationMetadataDto.class);
            application.setLatest(
                    new ApplicationMetadata()
                            .setVersion(dto.getBaseVersion())
                            .setRepositoryId(properties.getRepository())
                            .setSha1(dto.getSha1())
            );
            inputStream.close();
            return application;
        } catch (IOException ex) {
            throw new DeployActionException("Error downloading file", ex);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApplicationMetadataDto {
        private String baseVersion;
        private String sha1;
    }
}
