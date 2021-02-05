package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.en.Given;
import lombok.RequiredArgsConstructor;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.IntegrasjonspunktProperties;
import org.junit.Before;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RequiredArgsConstructor
public class DeploymanagerPropertiesSteps {

    private final DeployManagerProperties properties;

    private final IntegrasjonspunktProperties integrasjonspunktProperties;

    @Before
    public void setUp() {
        properties.setIntegrasjonspunkt(integrasjonspunktProperties);
    }

    @Given("the latest integrasjonspunkt version is \"([^\"]*)\"$")
    public void theLatestIntegrasjonspunktVersionIs(String version) {
        given(integrasjonspunktProperties.getLatestVersion()).willReturn(version);
    }
}
