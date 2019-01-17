package no.difi.move.deploymanager.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.application.*;
import no.difi.move.deploymanager.domain.application.Application;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultHandler implements AbstractHandler {

    private final GetCurrentVersionAction currentVersionAction;
    private final LatestVersionAction latestVersionAction;
    private final PrepareApplicationAction prepareApplicationAction;
    private final ValidateAction validateAction;
    private final ShutdownAction shutdownAction;
    private final StartAction startAction;
    private final RollbackAction rollbackAction;
    private final UpdateMetadataAction updateMetadataAction;

    @Override
    @Scheduled(fixedRateString = "${deploymanager.schedulerFixedRateInMs}")
    public void run() {
        log.debug("Starting synchronization.");
        currentVersionAction
                .andThen(latestVersionAction)
                .andThen(prepareApplicationAction)
                .andThen(validateAction)
                .andThen(shutdownAction)
                .andThen(startAction)
                .andThen(rollbackAction)
                .andThen(updateMetadataAction)
                .apply(new Application());
        log.debug("Finished synchronization.");
    }
}
