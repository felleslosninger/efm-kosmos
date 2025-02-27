package no.difi.move.kosmos.cucumber;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.service.config.RefreshService;
import no.difi.move.kosmos.service.config.VersionsConfig;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;

import static org.mockito.BDDMockito.given;

@RequiredArgsConstructor
public class DeploymanagerPropertiesSteps {

    private final KosmosProperties properties;
    private final IntegrasjonspunktProperties integrasjonspunktProperties;
    private final RefreshService refreshService;

    @BeforeEach
    public void setUp() {
        properties.setIntegrasjonspunkt(integrasjonspunktProperties);
    }

    @Given("^the latest integrasjonspunkt version is \"([^\"]*)\"$")
    public void theLatestIntegrasjonspunktVersionIs(String version) {
        var environments = new HashMap<String, VersionsConfig.Versions>();
        environments.put(integrasjonspunktProperties.getProfile(), new VersionsConfig.Versions(version, "9.9.9"));
        var versionsConfig = new VersionsConfig(new VersionsConfig.Environments(environments));
        given(refreshService.refreshConfig()).willReturn(versionsConfig);
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
        var environments = new HashMap<String, VersionsConfig.Versions>();
        environments.put(integrasjonspunktProperties.getProfile(), new VersionsConfig.Versions("0.0.0", version));
        var versionsConfig = new VersionsConfig(new VersionsConfig.Environments(environments));
        given(refreshService.refreshConfig()).willReturn(versionsConfig);
    }

}
