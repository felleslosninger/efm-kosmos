package no.difi.move.kosmos.service.config;

import java.util.Map;

public record VersionsConfig(Environments integrasjonspunkt) {

    public record Environments(Map<String, Versions> environments) {}
    public record Versions(String latest, String earlybird) {}

}


