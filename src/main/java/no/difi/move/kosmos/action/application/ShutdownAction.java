package no.difi.move.kosmos.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShutdownAction implements ApplicationAction {

    private final ActuatorService actuatorService;

    @Override
    public Application apply(Application application) {
        log.trace("Calling ShutdownAction.apply() on application {}", application);
        if (needToShutdown(application)) {
            doShutdown();
        }

        return application;
    }

    private boolean needToShutdown(Application application) {
        return application.getCurrent() != null
                && !application.isSameVersion()
                && actuatorService.getStatus() == HealthStatus.UP;
    }

    private void doShutdown() {
        log.info("Shutdown running version");
        if (!actuatorService.shutdown()) {
            log.warn("Shutdown failed!");
        }
    }
}

