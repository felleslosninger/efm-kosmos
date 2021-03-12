package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import no.difi.move.deploymanager.service.launcher.LauncherService;
import no.difi.move.deploymanager.service.launcher.dto.LaunchResult;
import no.difi.move.deploymanager.service.mail.MailService;
import org.springframework.stereotype.Component;

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
    public Application apply(Application application) {
        log.trace("Calling RollbackAction.apply() on application {}", application);
        if (shouldRollback(application)) {
            log.info("Rolling back to previous version");

            File jarFile = application.getCurrent().getFile();
            log.debug("Previous version: {}", application.getCurrent());
            LaunchResult launchResult = launcherService.launchIntegrasjonspunkt(jarFile.getAbsolutePath());
            log.debug("LaunchResult: {}", launchResult);
            String subject = String.format("Rollback %s %s", launchResult.getStatus().name(), jarFile.getName());

            log.info(subject);

            mailService.sendMail(
                    subject,
                    launchResult.getStartupLog()
            );

            return application
                    .setLaunchResult(launchResult)
                    .setLatest(application.getCurrent());
        }

        return application;
    }

    private boolean shouldRollback(Application application) {
        log.debug("Determining whether application {} should roll back", application);
        return actuatorService.getStatus() != HealthStatus.UP
                && application.getCurrent() != null
                && application.getCurrent().getFile() != null;
    }
}
