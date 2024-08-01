package no.difi.move.kosmos.service.actuator;

import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.domain.VersionInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActuatorServiceImplTest {

    @Mock
    private KosmosProperties properties;
    @Mock
    private ActuatorClient actuatorClient;

    @InjectMocks
    private ActuatorServiceImpl actuatorServiceImpl;

    @AfterEach
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
        given(properties.getShutdownRetries()).willReturn(3);
        given(properties.getShutdownPollIntervalInMs()).willReturn(1);
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
        given(properties.getShutdownRetries()).willReturn(3);
        given(properties.getShutdownPollIntervalInMs()).willReturn(1);
        given(actuatorClient.requestShutdown()).willReturn(true);
        given(actuatorClient.getStatus()).willReturn(HealthStatus.UP);

        assertThat(actuatorServiceImpl.shutdown()).isFalse();

        verify(actuatorClient).requestShutdown();
        verify(actuatorClient, times(3)).getStatus();
    }

    @Test
    public void testGetVersion() {
        given(actuatorClient.getVersionInfo()).willReturn(
                VersionInfo.builder()
                        .resolved(false)
                        .version(null)
                        .build(),
                VersionInfo.builder()
                        .resolved(true)
                        .version("2")
                        .build()
        );

        assertThat(actuatorServiceImpl.getVersionInfo().isResolved()).isFalse();
        assertThat(actuatorServiceImpl.getVersionInfo().isResolved()).isTrue();

        verify(actuatorClient, times(2)).getVersionInfo();
    }
}
