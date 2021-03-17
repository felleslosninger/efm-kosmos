package no.difi.move.deploymanager.service.actuator;

import lombok.SneakyThrows;
import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.VersionInfo;

import java.net.URISyntaxException;

public interface ActuatorClient {
    @SneakyThrows(URISyntaxException.class)
    HealthStatus getStatus();

    @SneakyThrows(URISyntaxException.class)
    boolean requestShutdown();

    @SneakyThrows(URISyntaxException.class)
    VersionInfo getVersionInfo();
}
