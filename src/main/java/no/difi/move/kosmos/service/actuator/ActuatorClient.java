package no.difi.move.kosmos.service.actuator;

import lombok.SneakyThrows;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.domain.VersionInfo;

import java.net.URISyntaxException;

public interface ActuatorClient {
    @SneakyThrows(URISyntaxException.class)
    HealthStatus getStatus();

    @SneakyThrows(URISyntaxException.class)
    boolean requestShutdown();

    @SneakyThrows(URISyntaxException.class)
    VersionInfo getVersionInfo();
}
