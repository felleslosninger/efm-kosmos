package no.difi.move.deploymanager.service.actuator;

import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.HealthStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ActuatorServiceImplTest {

    @Mock private DeployManagerProperties properties;
    @Mock private ActuatorClient actuatorClient;

    @InjectMocks private ActuatorServiceImpl actuatorServiceImpl;

    @Before
    public void before() {
        given(properties.getShutdownRetries()).willReturn(3);
        given(properties.getShutdownPollIntervalInMs()).willReturn(1);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(actuatorClient);
    }

    @Test
    public void testGetStatus() {
        given(actuatorClient.getStatus()).willReturn(HealthStatus.UP, HealthStatus.DOWN);

        assertThat(actuatorServiceImpl.getStatus()).isSameAs(HealthStatus.UP);
        assertThat(actuatorServiceImpl.getStatus()).isSameAs(HealthStatus.DOWN);

        verify(actuatorClient, times(2)).getStatus();
    }

    @Test
    public void testShutdownWhenRequestShutdownFailsAndHealthStatusIsDown() {
        given(actuatorClient.requestShutdown()).willReturn(false);
        given(actuatorClient.getStatus()).willReturn(HealthStatus.DOWN);

        assertThat(actuatorServiceImpl.shutdown()).isTrue();

        verify(actuatorClient).requestShutdown();
        verify(actuatorClient).getStatus();
    }

    @Test
    public void testShutdownWhenRequestShutdownFailsAndHealthStatusIsUp() {
        given(actuatorClient.requestShutdown()).willReturn(false);
        given(actuatorClient.getStatus()).willReturn(HealthStatus.UP);

        assertThat(actuatorServiceImpl.shutdown()).isFalse();

        verify(actuatorClient).requestShutdown();
        verify(actuatorClient).getStatus();
    }

    @Test
    public void testShutdownRetries() {
        given(actuatorClient.requestShutdown()).willReturn(true);
        given(actuatorClient.getStatus()).willReturn(
                HealthStatus.UP, HealthStatus.UP, HealthStatus.DOWN
        );

        assertThat(actuatorServiceImpl.shutdown()).isTrue();

        verify(actuatorClient).requestShutdown();
        verify(actuatorClient, times(3)).getStatus();
    }

    @Test
    public void testShutdownTimeout() {
        given(actuatorClient.requestShutdown()).willReturn(true);
        given(actuatorClient.getStatus()).willReturn(HealthStatus.UP);

        assertThat(actuatorServiceImpl.shutdown()).isFalse();

        verify(actuatorClient).requestShutdown();
        verify(actuatorClient, times(3)).getStatus();
    }
}
