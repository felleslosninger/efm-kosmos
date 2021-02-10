package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.IntegrasjonspunktProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class VerifyVersionComplianceActionTest {

    private static final IntegrasjonspunktProperties IP_PROPERTIES_MOCK = mock(IntegrasjonspunktProperties.class);
    private static final ApplicationMetadata METADATA_MOCK = mock(ApplicationMetadata.class);

    @Spy
    private final Application applicationSpy = new Application();

    @Mock
    private DeployManagerProperties properties;

    @InjectMocks
    private VerifyVersionComplianceAction target;

    @Before
    public void setUp() {
        given(properties.getIntegrasjonspunkt()).willReturn(IP_PROPERTIES_MOCK);
        given(applicationSpy.getLatest()).willReturn(METADATA_MOCK);
    }

    @Test
    public void apply_NoVersionToVerify_ShouldFailVerification() {
        given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn("2");
        given(METADATA_MOCK.getVersion()).willReturn(null);
        assertThatThrownBy(() -> target.apply(applicationSpy))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void apply_NoSupportedMajorVersionSet_ShouldPassVerification() {
        given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn(null);
        given(METADATA_MOCK.getVersion()).willReturn("2");
        assertThat(target.apply(applicationSpy)).isSameAs(applicationSpy);
    }

    @Test
    public void apply_UnsupportedMajorVersion_ShouldFailVerification() {
        given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn("1");
        given(METADATA_MOCK.getVersion()).willReturn("2.0.0");
        assertThatThrownBy(() -> target.apply(applicationSpy))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void apply_UnsupportedSnapshotVersion_ShouldFailVerification() {
        given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn("1");
        given(METADATA_MOCK.getVersion()).willReturn("2.0.0-SNAPSHOT");
        assertThatThrownBy(() -> target.apply(applicationSpy))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void apply_NonSemverVersion_ShouldFailVerification() {
        given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn("1");
        given(METADATA_MOCK.getVersion()).willReturn("beta");
        assertThatThrownBy(() -> target.apply(applicationSpy))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void apply_SupportedMajorVersion_ShouldPassVerification() {
        given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn("2");
        given(METADATA_MOCK.getVersion()).willReturn("2.3.4-SNAPSHOT");
        assertThat(target.apply(applicationSpy)).isSameAs(applicationSpy);
    }

}