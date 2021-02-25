package no.difi.move.deploymanager.service.actuator;

import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.VersionInfo;

public interface ActuatorService {

    HealthStatus getStatus();

    boolean shutdown();

    VersionInfo getVersionInfo();
}
