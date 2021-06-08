package no.difi.move.kosmos.domain;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class VersionInfo {
    boolean resolved;
    String version;
}
