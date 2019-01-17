package no.difi.move.deploymanager.domain;

import java.util.Arrays;

public enum HealthStatus {
    UP, DOWN, UNKOWN;

    public static HealthStatus fromString(String status) {
        return Arrays.stream(HealthStatus.values())
                .filter(p -> p.name().equalsIgnoreCase(status))
                .findFirst()
                .orElse(HealthStatus.UNKOWN);
    }
}
