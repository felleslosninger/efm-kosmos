package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.handler.SynchronizationHandler;

@Slf4j
@RequiredArgsConstructor
public class SynchronizationSteps {

    private final SynchronizationHandler synchronizationHandler;

    @Given("the synchronization handler is triggered")
    public void theSynchronizationHandlerIsTriggered() {
        try {
            synchronizationHandler.run();
        } catch (DeployActionException e) {
            log.warn("DeployActionException was thrown", e);
        }
    }
}
