package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.IntegrasjonspunktProperties;
import no.difi.move.deploymanager.domain.application.Application;
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

    @InjectMocks
    private LatestVersionAction target;

    @Test
    public void apply_receivesValidNexusResponse_shouldSetLatestVersion() {
        given(propertiesMock.getRepository()).willReturn("staging");
        IntegrasjonspunktProperties integrasjonspunktProperties = mock(IntegrasjonspunktProperties.class);
        given(integrasjonspunktProperties.getLatestVersion()).willReturn("latest");
        given(propertiesMock.getIntegrasjonspunkt()).willReturn(integrasjonspunktProperties);

        Application result = target.apply(new Application());

        assertThat(result.getLatest().getRepositoryId()).isEqualTo("staging");
        assertThat(result.getLatest().getVersion()).isEqualTo("latest");
    }
}
