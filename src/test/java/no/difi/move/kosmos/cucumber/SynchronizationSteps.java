package no.difi.move.kosmos.cucumber;

import cucumber.api.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.handler.SynchronizationHandler;

@Slf4j
@RequiredArgsConstructor
public class SynchronizationSteps {

    private final SynchronizationHandler synchronizationHandler;

    @Given("the synchronization handler is triggered")
    public void theSynchronizationHandlerIsTriggered() {
        try {
            synchronizationHandler.run();
        } catch (KosmosActionException e) {
            log.warn("KosmosActionException was thrown", e);
        }
    }
}
