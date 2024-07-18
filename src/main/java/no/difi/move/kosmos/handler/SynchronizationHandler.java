package no.difi.move.kosmos.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.application.*;
import no.difi.move.kosmos.domain.application.Application;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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

    @EventListener(ApplicationReadyEvent.class)
    public void initialRun() {
        scheduledRun();
    }

    @Scheduled(cron = "${kosmos.schedulerCronExpression}", zone = "Europe/Oslo")
    public void scheduledRun() {
        try {
            run();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void run() {
        log.info("Starting synchronization");
        currentVersionAction
                .andThen(latestVersionAction)
                .andThen(verifyVersionComplianceAction)
                .andThen(prepareApplicationAction)
                .andThen(validateAction)
                .andThen(shutdownAction)
                .andThen(startAction)
                .andThen(rollbackAction)
                .apply(new Application());
        log.info("Finished synchronization");
    }

}
