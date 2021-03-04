package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.en.Given;
import lombok.RequiredArgsConstructor;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.IntegrasjonspunktProperties;
import org.junit.Before;

import java.net.URL;

import static org.mockito.BDDMockito.given;

@RequiredArgsConstructor
public class DeploymanagerPropertiesSteps {

    private final DeployManagerProperties properties;
    private final IntegrasjonspunktProperties integrasjonspunktProperties;

    @Before
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


    @Given("^the shutdown URL is \"([^\"]*)\"$")
    public void theShutdownUrlIs(URL url){
        given(integrasjonspunktProperties.getShutdownURL()).willReturn(url);
    }

    @Given("^the health URL is \"([^\"]*)\"$")
    public void theHealthUrlIs(URL url){
        given(integrasjonspunktProperties.getHealthURL()).willReturn(url);
    }

    @Given("the info URL is \"([^\"]*)\"$")
    public void theInfoUrlIs(URL url){
        given(integrasjonspunktProperties.getInfoURL()).willReturn(url);
    }
}
