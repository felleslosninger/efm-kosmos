package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ShutdownActionTest {

    @InjectMocks
    private ShutdownAction target;

    @Mock
    private ActuatorService actuatorServiceMock;
    @Mock
    private Application applicationMock;

    @Test
    public void apply_applicationArgumentIsNull_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> target.apply(null));
    }

    @Test
    public void apply_currentVersionIsLatest_shouldNotShutdown() {
        given(applicationMock.getCurrent())
                .willReturn(new ApplicationMetadata().setVersion("old"));
        given(applicationMock.isSameVersion()).willReturn(true);

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(actuatorServiceMock, never()).shutdown();
    }

    @Test
    public void apply_currentVersionIsOldAndHealthStatusIsUp_shouldShutdown() {
        given(applicationMock.getCurrent())
                .willReturn(new ApplicationMetadata().setVersion("old"));
        given(applicationMock.isSameVersion()).willReturn(false);
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.UP);

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(actuatorServiceMock).shutdown();
    }

    @Test
    public void apply_currentVersionIsOldAndHealthStatusIsDown_shouldNotShutdwon() {
        given(applicationMock.getCurrent())
                .willReturn(new ApplicationMetadata().setVersion("old"));
        given(applicationMock.isSameVersion()).willReturn(false);
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(actuatorServiceMock, never()).shutdown();
    }
}
