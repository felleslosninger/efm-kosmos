package no.difi.move.deploymanager.domain;

import java.util.Arrays;

public enum HealthStatus {
    UP, DOWN, UNKNOWN;

    public static HealthStatus fromString(String status) {
        if (status == null) {
            return HealthStatus.UNKNOWN;
        }

        return Arrays.stream(HealthStatus.values())
                .filter(p -> p.name().equalsIgnoreCase(status))
                .findFirst()
                .orElse(HealthStatus.UNKNOWN);
    }
}
