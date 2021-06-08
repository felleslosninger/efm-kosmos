package no.difi.move.kosmos.service.actuator;

import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.domain.VersionInfo;

public interface ActuatorService {

    HealthStatus getStatus();

    boolean shutdown();

    VersionInfo getVersionInfo();
}
