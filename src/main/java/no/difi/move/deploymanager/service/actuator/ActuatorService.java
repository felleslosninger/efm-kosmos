package no.difi.move.deploymanager.service.actuator;

import no.difi.move.deploymanager.domain.HealthStatus;

public interface ActuatorService {

    HealthStatus getStatus();

    boolean shutdown();
}
