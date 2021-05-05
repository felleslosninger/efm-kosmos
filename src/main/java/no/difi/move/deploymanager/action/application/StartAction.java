package no.difi.move.deploymanager.action.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;
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

@Component
@Slf4j
@RequiredArgsConstructor
public class StartAction implements ApplicationAction {

    private final DeployManagerProperties properties;
    private final ActuatorService actuatorService;
    private final LauncherService launcherService;
    private final DeployDirectoryRepo deployDirectoryRepo;
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

        boolean blacklistEnabled = properties.getBlacklist().isEnabled();
        if (!blacklistEnabled){
            log.info("Blacklist mechanism is disabled");
        }
        if (blacklistEnabled && launchResult.getStatus() != LaunchStatus.SUCCESS) {
            log.info("Launch failed, the version will be blacklisted");
            deployDirectoryRepo.blackList(jarFile);

            if (actuatorService.getStatus() == HealthStatus.UP) {
                log.trace("The application started in the mean time, but is now shutting down");
                actuatorService.shutdown();
            }
        }

        if(launchResult.getStatus() == LaunchStatus.SUCCESS) {
            log.info("Launch success, the version {} will be whitelisted", application.getLatest().getVersion());
            String version = deployDirectoryRepo.getWhitelistVersion();
            if(version != null) {
                deployDirectoryRepo.removeWhitelist(deployDirectoryRepo.getFile(version, "integrasjonspunkt-%s.whitelisted"));
            }
            deployDirectoryRepo.whitelist(jarFile, String.format("integrasjonspunkt-%s.whitelisted", application.getLatest().getVersion()));
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
