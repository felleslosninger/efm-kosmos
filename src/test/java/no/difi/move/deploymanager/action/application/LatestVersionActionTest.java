package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.IntegrasjonspunktProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.service.config.RefreshService;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class LatestVersionActionTest {

    @Mock
    private DeployManagerProperties propertiesMock;
    @Mock
    private RefreshService refreshServiceMock;

    @Before
    public void setUp() {
        integrasjonspunktProperties = mock(IntegrasjonspunktProperties.class);
        given(propertiesMock.getIntegrasjonspunkt()).willReturn(integrasjonspunktProperties);
    }

    private IntegrasjonspunktProperties integrasjonspunktProperties;

    @InjectMocks
    private LatestVersionAction target;

    @Test
    public void apply_LatestVersionFound_ShouldSetLatestVersion() {
        given(integrasjonspunktProperties.getLatestVersion()).willReturn("latest");
        Application result = target.apply(new Application());
        assertThat(result.getLatest().getVersion()).isEqualTo("latest");
    }

    @Test(expected = DeployActionException.class)
    public void apply_NullPointerExceptionOccurs_ShouldThrowDeployActionException() {
        given(propertiesMock.getIntegrasjonspunkt()).willThrow(NullPointerException.class);
        target.apply(new Application());
    }

    @Test
    public void apply_EarlyBirdIsTrueButVersionIsNull_ShouldDefaultToLatestVersion() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(true);
        given(integrasjonspunktProperties.getEarlyBirdVersion()).willReturn(null);
        given(integrasjonspunktProperties.getLatestVersion()).willReturn("Latest");

        Application result = target.apply(new Application());

        assertThat(StringUtils.equals("Latest", result.getLatest().getVersion())).isTrue();
    }

    @Test
    public void apply_EarlyBirdIsFalseButVersionIsSet_ShouldSetLatestVersion() {
        IntegrasjonspunktProperties integrasjonspunktProperties = new IntegrasjonspunktProperties();
        integrasjonspunktProperties.setEarlyBird(false);
        integrasjonspunktProperties.setEarlyBirdVersion("EarlyBird");
        integrasjonspunktProperties.setLatestVersion("Latest");
        given(propertiesMock.getIntegrasjonspunkt()).willReturn(integrasjonspunktProperties);

        Application result = target.apply(new Application());

        assertThat(StringUtils.equals("Latest", result.getLatest().getVersion())).isTrue();
    }

    @Test
    public void apply_EarlyBirdIsTrueAndVersionSet_ShouldSetEarlyBirdVersion() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(true);
        given(integrasjonspunktProperties.getEarlyBirdVersion()).willReturn("EarlyBird");

        Application result = target.apply(new Application());

        assertThat(StringUtils.equals("EarlyBird", result.getLatest().getVersion())).isTrue();
    }
}
