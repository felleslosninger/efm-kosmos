package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.en.Given;
import lombok.RequiredArgsConstructor;
import no.difi.move.deploymanager.handler.SynchronizationHandler;

@RequiredArgsConstructor
public class SynchronizationSteps {

    private final SynchronizationHandler synchronizationHandler;

    @Given("the synchronization handler is triggered")
    public void theSynchronizationHandlerIsTriggered() {
        synchronizationHandler.run();
    }
}
