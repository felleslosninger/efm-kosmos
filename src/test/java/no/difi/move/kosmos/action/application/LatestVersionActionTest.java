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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private HashMap<String, VersionsConfig.Versions> environments;

    @InjectMocks
    private LatestVersionAction target;

    @BeforeEach
    public void setUp() {
        environments = new HashMap<>();
        environments.put("profile_ok", new VersionsConfig.Versions("2.31.0", "v4.2.0"));
        environments.put("profile_earlybird_null", new VersionsConfig.Versions("2.30.0", null));
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
        assertThat(result.getLatest().getVersion()).isEqualTo("2.31.0");
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
        assertThat(StringUtils.equals("2.30.0", result.getLatest().getVersion())).isTrue();
    }

    @Test
    public void apply_EarlyBirdIsFalseButVersionIsSet_ShouldSetLatestVersion() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(false);
        given(integrasjonspunktProperties.getProfile()).willReturn("profile_ok");
        Application result = target.apply(new Application());
        assertThat(StringUtils.equals("2.31.0", result.getLatest().getVersion())).isTrue();
    }

    @Test
    public void apply_EarlyBirdIsTrueAndVersionSet_ShouldSetEarlyBirdVersion() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(true);
        given(integrasjonspunktProperties.getProfile()).willReturn("profile_ok");
        Application result = target.apply(new Application());
        assertThat(StringUtils.equals("v4.2.0", result.getLatest().getVersion())).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"x/../../tmp/test", "../4.2.0", "4.2.0/evil", "4.2.0\\evil", "latest", "4.2", "4.2.0 "})
    public void apply_InvalidLatestVersion_ShouldThrow(String version) {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(false);
        given(integrasjonspunktProperties.getProfile()).willReturn("profile_invalid");
        environments.put("profile_invalid", new VersionsConfig.Versions(version, null));
        Application application = new Application();
        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
        assertThat(application.getLatest()).isNull();
    }

    @Test
    public void apply_InvalidEarlyBirdVersion_ShouldThrow() {
        given(integrasjonspunktProperties.isEarlyBird()).willReturn(true);
        given(integrasjonspunktProperties.getProfile()).willReturn("profile_invalid");
        environments.put("profile_invalid", new VersionsConfig.Versions("2.31.0", "x/../../tmp/test"));
        Application application = new Application();
        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(KosmosActionException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
        assertThat(application.getLatest()).isNull();
    }

}
