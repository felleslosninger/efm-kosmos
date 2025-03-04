package no.difi.move.kosmos.service.launcher;

import lombok.SneakyThrows;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.service.launcher.dto.LaunchResult;
import no.difi.move.kosmos.service.launcher.dto.LaunchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LauncherServiceImplTest {

    @Mock
    private KosmosProperties properties;
    @Mock
    private ActuatorService actuatorService;
    @Mock
    private EnvironmentService environmentService;
    @Mock
    private StartedProcess startedProcessMock;
    @Mock
    private Future<ProcessResult> futureMock;
    @Mock
    private File fileMock;

    @TempDir
    static Path tempDir;

    @InjectMocks
    private LauncherServiceImpl launcherService;

    @BeforeEach
    @SneakyThrows
    public void before() {
        IntegrasjonspunktProperties integrasjonspunktProperties = mock(IntegrasjonspunktProperties.class);
        given(properties.getIntegrasjonspunkt()).willReturn(integrasjonspunktProperties);
        given(properties.getIntegrasjonspunkt()).willReturn(
                new IntegrasjonspunktProperties()
                        .setProfile("staging")
                        .setHome(tempDir.toString())
        );
    }

    @Test
    @SneakyThrows
    public void testLaunchIntegrasjonspunkt_whenSuccess() {
        given(properties.getLaunchPollIntervalInMs()).willReturn(100);
        given(properties.getLaunchTimeoutInMs()).willReturn(300);
        given(actuatorService.getStatus()).willReturn(HealthStatus.UP);
        given(environmentService.getChildProcessEnvironment()).willReturn(new HashMap<>());
        given(startedProcessMock.getFuture()).willReturn(futureMock);
        try (MockedConstruction<StartupLog> startupLogMock = mockConstruction(StartupLog.class,
                (mock, context) -> {
                    when(mock.getLog()).thenReturn("theStartUpLog");
                });
             MockedConstruction<ProcessExecutor> executorMock = mockConstruction(ProcessExecutor.class,
                     (mock, context) -> {
                         when(mock.directory(any())).thenReturn(mock);
                         when(mock.environment(any())).thenReturn(mock);
                         when(mock.redirectOutput(any())).thenReturn(mock);
                         when(mock.start()).thenReturn(startedProcessMock);
                     })) {

            LaunchResult result = launcherService.launchIntegrasjonspunkt("test.jar");

            assertThat(result)
                    .hasFieldOrPropertyWithValue("jarPath", "test.jar")
                    .hasFieldOrPropertyWithValue("status", LaunchStatus.SUCCESS)
                    .hasFieldOrPropertyWithValue("startupLog", "theStartUpLog");
            verify(futureMock, never()).cancel(anyBoolean());
        }
    }

    @Test
    @SneakyThrows
    public void testLaunchIntegrasjonspunkt_whenActuatorReturnsFailure() {
        given(properties.getLaunchPollIntervalInMs()).willReturn(100);
        given(properties.getLaunchTimeoutInMs()).willReturn(300);
        given(startedProcessMock.getFuture()).willReturn(futureMock);
        given(actuatorService.getStatus()).willReturn(HealthStatus.UNKNOWN);

        try (MockedConstruction<StartupLog> startupLogMock = mockConstruction(StartupLog.class,
                (mock, context) -> {
                    when(mock.getLog()).thenReturn("theStartUpLog");
                });
             MockedConstruction<ProcessExecutor> executorMock = mockConstruction(ProcessExecutor.class,
                     (mock, context) -> {
                         when(mock.directory(any())).thenReturn(mock);
                         when(mock.environment(any())).thenReturn(mock);
                         when(mock.redirectOutput(any())).thenReturn(mock);
                         when(mock.start()).thenReturn(startedProcessMock);
                     })) {

            LaunchResult result = launcherService.launchIntegrasjonspunkt("test.jar");

            assertThat(result)
                    .hasFieldOrPropertyWithValue("jarPath", "test.jar")
                    .hasFieldOrPropertyWithValue("status", LaunchStatus.FAILED)
                    .hasFieldOrPropertyWithValue("startupLog", "theStartUpLog");
        }
    }

    @Test
    public void testLaunchIntegrasjonspunkt_whenIOException() {
        try (MockedConstruction<StartupLog> startupLogMock = mockConstruction(StartupLog.class,
                (mock, context) -> {
                    when(mock.getLog()).thenReturn("theStartUpLog");
                });
             MockedConstruction<ProcessExecutor> executorMock = mockConstruction(ProcessExecutor.class,
                     (mock, context) -> {
                         when(mock.directory(any())).thenReturn(mock);
                         when(mock.environment(any())).thenReturn(mock);
                         when(mock.redirectOutput(any())).thenReturn(mock);
                         when(mock.start()).thenThrow(new IOException("test exception"));
                     })) {
            assertThat(launcherService.launchIntegrasjonspunkt("test.jar"))
                    .hasFieldOrPropertyWithValue("jarPath", "test.jar")
                    .hasFieldOrPropertyWithValue("status", LaunchStatus.FAILED)
                    .hasFieldOrPropertyWithValue("startupLog", "test exception");
        }
    }

}
