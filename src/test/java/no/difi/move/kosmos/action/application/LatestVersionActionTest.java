package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.service.config.RefreshService;
import no.difi.move.kosmos.service.config.VersionsConfig;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

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

    private IntegrasjonspunktProperties integrasjonspunktProperties;

    @InjectMocks
    private LatestVersionAction target;

    @BeforeEach
    public void setUp() {
        var environments = new HashMap<String, VersionsConfig.Versions>();
        environments.put("profile_ok", new VersionsConfig.Versions("latest", "EarlyBird"));
        environments.put("profile_earlybird_null", new VersionsConfig.Versions("Latest", null));
        var versionsConfig = new VersionsConfig(new VersionsConfig.Environments(environments));
        integrasjonspunktProperties = mock(IntegrasjonspunktProperties.class);
        given(propertiesMock.getIntegrasjonspunkt()).willReturn(integrasjonspunktProperties);
        given(refreshServiceMock.refreshConfig()).willReturn(versionsConfig);
    }

    @Test
    public void apply_LatestVersionFound_ShouldSetLatestVersion() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(false);
        given(integrasjonspunktProperties.getProfile()).willReturn("profile_ok");
        Application result = target.apply(new Application());
        assertThat(result.getLatest().getVersion()).isEqualTo("latest");
    }

    @Test
    public void apply_NullPointerExceptionOccurs_ShouldThrowDeployActionException() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(true);
        given(integrasjonspunktProperties.getProfile()).willReturn("profile_non_existent");
        assertThrows(KosmosActionException.class,
                () -> target.apply(new Application())
        );
    }

    @Test
    public void apply_EarlyBirdIsTrueButVersionIsNull_ShouldDefaultToLatestVersion() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(true);
        given(integrasjonspunktProperties.getProfile()).willReturn("profile_earlybird_null");
        Application result = target.apply(new Application());
        assertThat(StringUtils.equals("Latest", result.getLatest().getVersion())).isTrue();
    }

    @Test
    public void apply_EarlyBirdIsFalseButVersionIsSet_ShouldSetLatestVersion() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(false);
        given(integrasjonspunktProperties.getProfile()).willReturn("profile_ok");
        Application result = target.apply(new Application());
        assertThat(StringUtils.equals("latest", result.getLatest().getVersion())).isTrue();
    }

    @Test
    public void apply_EarlyBirdIsTrueAndVersionSet_ShouldSetEarlyBirdVersion() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(true);
        given(integrasjonspunktProperties.getProfile()).willReturn("profile_ok");
        Application result = target.apply(new Application());
        assertThat(StringUtils.equals("EarlyBird", result.getLatest().getVersion())).isTrue();
    }

}
