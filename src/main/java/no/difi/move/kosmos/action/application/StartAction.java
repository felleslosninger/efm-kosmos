package no.difi.move.kosmos.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.repo.KosmosDirectoryRepo;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.service.launcher.LauncherService;
import no.difi.move.kosmos.service.launcher.dto.LaunchResult;
import no.difi.move.kosmos.service.launcher.dto.LaunchStatus;
import no.difi.move.kosmos.service.mail.MailService;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
@RequiredArgsConstructor
public class StartAction implements ApplicationAction {

    private final KosmosProperties properties;
    private final ActuatorService actuatorService;
    private final LauncherService launcherService;
    private final KosmosDirectoryRepo kosmosDirectoryRepo;
    private final MailService mailService;

    @Override
    public Application apply(Application application) {
    log.trace("Calling StartAction.apply() on application: {}", application);

        if (isAlreadyRunning(application)) {
            log.info("The application is already running");
            return application;
        }

        File jarFile = application.getLatest().getFile();
        LaunchResult launchResult = launcherService.launchIntegrasjonspunkt(jarFile.getAbsolutePath());

        boolean blocklistEnabled = properties.getBlocklist().isEnabled();
        if (!blocklistEnabled){
            log.info("Blocklist mechanism is disabled");
        }
        if (blocklistEnabled && launchResult.getStatus() != LaunchStatus.SUCCESS) {
            log.info("Launch failed, the version will be blocklisted");
            kosmosDirectoryRepo.blockList(jarFile);

            if (actuatorService.getStatus() == HealthStatus.UP) {
                log.trace("The application started in the mean time, but is now shutting down");
                actuatorService.shutdown();
            }
        }

        if(launchResult.getStatus() == LaunchStatus.SUCCESS) {
            log.info("Launch success, the version {} will be Allowlisted", application.getLatest().getVersion());
            String version = kosmosDirectoryRepo.getAllowlistVersion();
            if(version != null) {
            kosmosDirectoryRepo.removeAllowlist(version);
            }
            kosmosDirectoryRepo.allowlist(jarFile, application.getLatest().getVersion());
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
        return application.getCurrent() != null
                && application.isSameVersion()
                && actuatorService.getStatus() == HealthStatus.UP;
    }
}
