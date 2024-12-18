package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.service.config.RefreshService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class LatestVersionActionTest {

    @Mock
    private KosmosProperties propertiesMock;
    @Mock
    private RefreshService refreshServiceMock;

    @BeforeEach
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

    @Test
    public void apply_NullPointerExceptionOccurs_ShouldThrowDeployActionException() {
        given(propertiesMock.getIntegrasjonspunkt()).willThrow(NullPointerException.class);

        assertThrows(KosmosActionException.class,
                () -> target.apply(new Application())
        );
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
