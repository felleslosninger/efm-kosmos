package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import no.difi.move.deploymanager.service.launcher.LauncherService;
import no.difi.move.deploymanager.service.launcher.dto.LaunchResult;
import no.difi.move.deploymanager.service.launcher.dto.LaunchStatus;
import no.difi.move.deploymanager.service.mail.MailService;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StartAction implements ApplicationAction {

    private final ActuatorService actuatorService;
    private final LauncherService launcherService;
    private final DeployDirectoryRepo deployDirectoryRepo;
    private final MailService mailService;

    @Override
    public Application apply(Application application) {
        log.debug("Running StartAction.");

        if (isAlreadyRunning(application)) {
            log.info("The application is already running.");
            return application;
        }

        File jarFile = application.getLatest().getFile();
        LaunchResult launchResult = launcherService.launchIntegrasjonspunkt(jarFile.getAbsolutePath());

        if (launchResult.getStatus() != LaunchStatus.SUCCESS) {
            deployDirectoryRepo.blackList(jarFile);

            if (actuatorService.getStatus() == HealthStatus.UP) {
                actuatorService.shutdown();
            }
        }

        String subject = String.format("Upgrade %s %s", launchResult.getStatus().name(), jarFile.getName());

        log.info(subject);

        mailService.sendMail(
                subject,
                launchResult.getStartupLog()
        );

        application.setLaunchResult(launchResult);

        return application;
    }

    private boolean isAlreadyRunning(Application application) {
        return application.isSameVersion() && actuatorService.getStatus() == HealthStatus.UP;
    }
}
