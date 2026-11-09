package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.service.launcher.LauncherService;
import no.difi.move.kosmos.service.launcher.dto.LaunchResult;
import no.difi.move.kosmos.service.launcher.dto.LaunchStatus;
import no.difi.move.kosmos.service.mail.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RollbackActionTest {

    @InjectMocks
    private RollbackAction target;

    @Mock
    private ActuatorService actuatorServiceMock;
    @Mock
    private LauncherService launcherServiceMock;
    @Mock
    private MailService mailService;
    @Mock
    private ApplicationMetadata currentMock;
    @Mock
    private File fileMock;

    private Application application;

    @BeforeEach
    public void before() {
        application = new Application().setCurrent(currentMock);
    }

    @Test
    public void apply_toNull_shouldThrow() {
        assertThrows(NullPointerException.class, () -> target.apply(null));
    }

    @Test
    public void apply_healthStatusIsUp_shouldNotRollback() {
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.UP);

        assertThat(target.apply(application)).isSameAs(application);

        verify(launcherServiceMock, never()).launchIntegrasjonspunkt(any(), any());
        verify(mailService, never()).sendMail(any(), any());
    }

    @Test
    public void apply_healthStatusIsDownAndFileIsSet_shouldRollback() {
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);
        given(currentMock.getVersion()).willReturn("1.0.0");
        given(currentMock.getFile()).willReturn(fileMock);
        given(fileMock.getName()).willReturn("test.jar");
        given(launcherServiceMock.launchIntegrasjonspunkt(any(), any())).willReturn(
                new LaunchResult()
                        .setStatus(LaunchStatus.SUCCESS)
                        .setStartupLog("theStartupLog")
        );

        assertThat(target.apply(application)).isSameAs(application);

        verify(launcherServiceMock).launchIntegrasjonspunkt("1.0.0", fileMock);
        verify(mailService).sendMail("Rollback SUCCESS test.jar", "theStartupLog");
    }

    @Test
    public void apply_rollbackJarIsNotVerified_shouldReportFailedRollback() {
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);
        given(currentMock.getVersion()).willReturn("1.0.0");
        given(currentMock.getFile()).willReturn(fileMock);
        given(fileMock.getName()).willReturn("test.jar");
        given(launcherServiceMock.launchIntegrasjonspunkt("1.0.0", fileMock)).willReturn(
                new LaunchResult()
                        .setStatus(LaunchStatus.FAILED)
                        .setStartupLog("The application was not started. Invalid signature")
        );

        assertThat(target.apply(application).getLaunchResult().getStatus()).isEqualTo(LaunchStatus.FAILED);

        verify(mailService).sendMail("Rollback FAILED test.jar", "The application was not started. Invalid signature");
    }

    @Test
    public void apply_healthStatusIsDownAndFileIsNotSet_shouldNotRollback() {
        given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);
        given(currentMock.getFile()).willReturn(null);

        assertThat(target.apply(application)).isSameAs(application);

        verify(launcherServiceMock, never()).launchIntegrasjonspunkt(any(), any());
    }
}
