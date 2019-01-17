package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import no.difi.move.deploymanager.service.laucher.LauncherService;
import no.difi.move.deploymanager.service.laucher.dto.LaunchResult;
import no.difi.move.deploymanager.service.mail.MailService;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.File;

/**
 * Performs rollback if the new version will not start
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RollbackAction implements ApplicationAction {

    private final ActuatorService actuatorService;
    private final LauncherService launcherService;
    private final MailService mailService;

    @Override
    public Application apply(@NotNull Application application) {
        log.debug("Running RollbackAction.");
        if (shouldRollback(application)) {
            log.info("Rolling back.");

            File jarFile = application.getCurrent().getFile();
            LaunchResult launchResult = launcherService.launchIntegrasjonspunkt(jarFile.getAbsolutePath());

            mailService.sendMail(
                    String.format("Rollback %s %s", launchResult.getStatus().name(), jarFile.getName()),
                    launchResult.getStartupLog()
            );

            return application
                    .setLaunchResult(launchResult)
                    .setLatest(application.getCurrent());
        }

        return application;
    }

    private boolean shouldRollback(Application application) {
        return actuatorService.getStatus() != HealthStatus.UP && application.getCurrent().getFile() != null;
    }
}
