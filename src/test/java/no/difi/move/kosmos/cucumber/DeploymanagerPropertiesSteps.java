package no.difi.move.kosmos.cucumber;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import no.difi.move.kosmos.config.KosmosProperties;
import org.junit.jupiter.api.BeforeEach;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;

import static org.mockito.BDDMockito.given;

@RequiredArgsConstructor
public class DeploymanagerPropertiesSteps {

    private final KosmosProperties properties;
    private final IntegrasjonspunktProperties integrasjonspunktProperties;

    @BeforeEach
    public void setUp() {
        properties.setIntegrasjonspunkt(integrasjonspunktProperties);
    }

    @Given("^the latest integrasjonspunkt version is \"([^\"]*)\"$")
    public void theLatestIntegrasjonspunktVersionIs(String version) {
        given(integrasjonspunktProperties.getLatestVersion()).willReturn(version);
    }

    @Given("^the supported major version is unset$")
    public void theCurrentSupportedMajorVersionIsNull(){
        given(integrasjonspunktProperties.getSupportedMajorVersion()).willReturn(null);
    }

    @Given("^the supported major version is \"([^\"]*)\"$")
    public void theCurrentSupportedMajorVersionIs(String majorVersion) {
        given(integrasjonspunktProperties.getSupportedMajorVersion()).willReturn(majorVersion);
    }

    @Given("^the early bird setting is not activated$")
    public void theEarlyBirdSettingIsNotActivated(){
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(false);
    }

    @Given("^the early bird setting is activated with version set to \"([^\"]*)\"$")
    public void theEarlyBirdSettingIsActivated(String version){
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(true);
        given(integrasjonspunktProperties.getEarlyBirdVersion()).willReturn(version);
    }
}
