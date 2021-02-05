package no.difi.move.deploymanager.repo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationMetadataResource {

    private String baseVersion;
    private String sha1;
}
