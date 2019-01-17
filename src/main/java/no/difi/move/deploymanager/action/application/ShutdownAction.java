package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShutdownAction implements ApplicationAction {

    private final ActuatorService actuatorService;

    @Override
    public Application apply(@NotNull Application application) {
        log.debug("Running ShutdownAction.");
        if (needToShutdown(application)) {
            doShutdown();
        }

        return application;
    }

    private boolean needToShutdown(Application application) {
        return !application.isSameVersion() && actuatorService.getStatus() == HealthStatus.UP;
    }

    private void doShutdown() {
        log.info("Shutdown running version.");
        if (!actuatorService.shutdown()) {
            log.warn("Shutdown failed!");
        }
    }
}

