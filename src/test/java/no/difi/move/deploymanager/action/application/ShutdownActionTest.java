package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ShutdownAction.class})
public class ShutdownActionTest {

    @InjectMocks private ShutdownAction target;

    @Mock private ActuatorService actuatorServiceMock;
    @Mock private Application applicationMock;

    @Test(expected = NullPointerException.class)
    public void apply_applicationArgumentIsNull_shouldThrow() {
        target.apply(null);
    }

    @Test
    public void apply_currentVersionIsLatest_shouldNotShutdown() {
        given(applicationMock.isSameVersion()).willReturn(true);
        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);
        verify(actuatorServiceMock, never()).shutdown();
    }

    @Test
    public void apply_currentVersionIsOldAndHealthStatusIsUp_shouldShutdown() {
        given(applicationMock.isSameVersion()).willReturn(false);
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.UP);
        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);
        verify(actuatorServiceMock).shutdown();
    }

    @Test
    public void apply_currentVersionIsOldAndHealthStatusIsDown_shouldNotShutdwon() {
        given(applicationMock.isSameVersion()).willReturn(false);
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);
        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);
        verify(actuatorServiceMock, never()).shutdown();
    }
}
