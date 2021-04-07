package no.difi.move.deploymanager.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.application.*;
import no.difi.move.deploymanager.domain.application.Application;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SynchronizationHandler {

    private final GetCurrentVersionAction currentVersionAction;
    private final LatestVersionAction latestVersionAction;
    private final VerifyVersionComplianceAction verifyVersionComplianceAction;
    private final PrepareApplicationAction prepareApplicationAction;
    private final ValidateAction validateAction;
    private final ShutdownAction shutdownAction;
    private final StartAction startAction;
    private final RollbackAction rollbackAction;

    @Scheduled(cron = "${deploymanager.schedulerCronExpression}", zone = "Europe/Oslo")
    public void run() {
        log.debug("Starting synchronization");
        currentVersionAction
                .andThen(latestVersionAction)
                .andThen(verifyVersionComplianceAction)
                .andThen(prepareApplicationAction)
                .andThen(validateAction)
                .andThen(shutdownAction)
                .andThen(startAction)
                .andThen(rollbackAction)
                .apply(new Application());
        log.debug("Finished synchronization");
    }
}
