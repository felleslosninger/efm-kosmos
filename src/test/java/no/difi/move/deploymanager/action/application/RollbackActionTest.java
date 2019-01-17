package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import no.difi.move.deploymanager.service.laucher.LauncherService;
import no.difi.move.deploymanager.service.laucher.dto.LaunchResult;
import no.difi.move.deploymanager.service.laucher.dto.LaunchStatus;
import no.difi.move.deploymanager.service.mail.MailService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RollbackActionTest {

    @InjectMocks private RollbackAction target;

    @Mock private ActuatorService actuatorServiceMock;
    @Mock private LauncherService launcherServiceMock;
    @Mock private MailService mailService;
    @Mock private ApplicationMetadata currentMock;
    @Mock private File fileMock;

    private Application application;

    @Before
    public void before() {
        application = new Application().setCurrent(currentMock);
        given(currentMock.getFile()).willReturn(fileMock);
        given(fileMock.getAbsolutePath()).willReturn("the path");
        given(fileMock.getName()).willReturn("test.jar");
        given(launcherServiceMock.launchIntegrasjonspunkt(any())).willReturn(
                new LaunchResult()
                        .setStatus(LaunchStatus.SUCCESS)
                        .setStartupLog("theStartupLog")
        );
    }

    @Test(expected = NullPointerException.class)
    public void apply_toNull_shouldThrow() {
        target.apply(null);
    }

    @Test
    public void apply_healthStatusIsUp_shouldNotRollback() {
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.UP);
        assertThat(target.apply(application)).isSameAs(application);
        verify(launcherServiceMock, never()).launchIntegrasjonspunkt(any());
        verify(mailService, never()).sendMail(any(), any());
    }

    @Test
    public void apply_healthStatusIsDownAndFileIsSet_shouldRollback() {
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);
        assertThat(target.apply(application)).isSameAs(application);
        verify(launcherServiceMock).launchIntegrasjonspunkt("the path");
        verify(mailService).sendMail("Rollback SUCCESS test.jar", "theStartupLog");
    }

    @Test
    public void apply_healthStatusIsDownAndFileIsNotSet_shouldNotRollback() {
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);
        given(currentMock.getFile()).willReturn(null);
        assertThat(target.apply(application)).isSameAs(application);
        verify(launcherServiceMock, never()).launchIntegrasjonspunkt(any());
    }
}
