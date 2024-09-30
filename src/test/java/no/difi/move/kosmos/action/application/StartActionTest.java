package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.config.BlocklistProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryRepo;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.service.launcher.LauncherService;
import no.difi.move.kosmos.service.launcher.dto.LaunchResult;
import no.difi.move.kosmos.service.launcher.dto.LaunchStatus;
import no.difi.move.kosmos.service.mail.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StartActionTest {

    @InjectMocks
    private StartAction target;

    @Mock
    private KosmosProperties propertiesMock;
    @Mock
    private ActuatorService actuatorServiceMock;
    @Mock
    private LauncherService launcherServiceMock;
    @Mock
    private KosmosDirectoryRepo kosmosDirectoryRepoMock;
    @Mock
    private MailService mailService;
    @Mock
    private Application applicationMock;
    @Mock
    private ApplicationMetadata latestMock;
    @Mock
    private File fileMock;
    @Mock
    private BlocklistProperties blocklistProperties;

    @Nested
    @DisplayName("Start fails")
    class StartFailsTests {

        @BeforeEach
        public void before() {
            given(blocklistProperties.isEnabled()).willReturn(true);
            given(propertiesMock.getBlocklist()).willReturn(blocklistProperties);
            given(applicationMock.getLatest()).willReturn(latestMock);
            given(latestMock.getFile()).willReturn(fileMock);
            given(fileMock.getName()).willReturn("test.jar");
            given(launcherServiceMock.launchIntegrasjonspunkt(any())).willReturn(
                    new LaunchResult()
                            .setStatus(LaunchStatus.SUCCESS)
                            .setStartupLog("theStartupLog")
            );
        }

        @Test
        @DisplayName("Application running, blocklist disabled, should not shutdown or blocklist")
        public void apply_StartFailsAndTheApplicationIsRunningAndBlocklistIsDisabled_JarShouldNotBeBlocklistedAndAShutdownIsNotTriggered() {
            given(launcherServiceMock.launchIntegrasjonspunkt(any())).willReturn(
                    new LaunchResult()
                            .setStatus(LaunchStatus.FAILED)
                            .setStartupLog("theStartupLog")
            );

            given(applicationMock.getCurrent())
                    .willReturn(new ApplicationMetadata().setVersion("latest"));
            given(applicationMock.isSameVersion()).willReturn(true);
            given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN, HealthStatus.UP);
            given(fileMock.getAbsolutePath()).willReturn("the path");
            given(blocklistProperties.isEnabled()).willReturn(false);

            assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

            verify(launcherServiceMock).launchIntegrasjonspunkt("the path");
            verify(mailService).sendMail("Upgrade FAILED test.jar", "theStartupLog");
            verify(kosmosDirectoryRepoMock, never()).blockList(fileMock);
            verify(actuatorServiceMock, never()).shutdown();
        }

        @Test
        @DisplayName("Blocklist latest version")
        public void apply_StartFailsAndTheApplicationIsRunning_JarFileShouldBeBlocklistedAndAShutdownTriggered() {
            given(launcherServiceMock.launchIntegrasjonspunkt(any())).willReturn(
                    new LaunchResult()
                            .setStatus(LaunchStatus.FAILED)
                            .setStartupLog("theStartupLog")
            );

            given(applicationMock.getCurrent())
                    .willReturn(new ApplicationMetadata().setVersion("latest"));
            given(applicationMock.isSameVersion()).willReturn(true);
            given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN, HealthStatus.UP);
            given(fileMock.getAbsolutePath()).willReturn("the path");

            assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

            verify(launcherServiceMock).launchIntegrasjonspunkt("the path");
            verify(mailService).sendMail("Upgrade FAILED test.jar", "theStartupLog");
            verify(kosmosDirectoryRepoMock).blockList(fileMock);
            verify(actuatorServiceMock).shutdown();
        }

        @Test
        @DisplayName("Block failing version")
        public void apply_whenStartFails_theJarFileShouldBeBlocklisted() {
            given(launcherServiceMock.launchIntegrasjonspunkt(any())).willReturn(
                    new LaunchResult()
                            .setStatus(LaunchStatus.FAILED)
                            .setStartupLog("theStartupLog")
            );

            given(applicationMock.getCurrent())
                    .willReturn(new ApplicationMetadata().setVersion("latest"));
            given(applicationMock.isSameVersion()).willReturn(true);
            given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);
            given(fileMock.getAbsolutePath()).willReturn("the path");
            assertThat(target.apply(applicationMock)).isSameAs(applicationMock);
            verify(launcherServiceMock).launchIntegrasjonspunkt("the path");
            verify(mailService).sendMail("Upgrade FAILED test.jar", "theStartupLog");
            verify(kosmosDirectoryRepoMock).blockList(fileMock);
        }

        @Test
        @DisplayName("Blocklist is disabled")
        public void apply_StartFailsAndBlocklistIsDisabled_JarFileShouldNotBeBlocklisted() {
            given(launcherServiceMock.launchIntegrasjonspunkt(any())).willReturn(
                    new LaunchResult()
                            .setStatus(LaunchStatus.FAILED)
                            .setStartupLog("theStartupLog")
            );

            given(applicationMock.getCurrent())
                    .willReturn(new ApplicationMetadata().setVersion("latest"));
            given(applicationMock.isSameVersion()).willReturn(true);
            given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);
            given(fileMock.getAbsolutePath()).willReturn("the path");
            given(blocklistProperties.isEnabled()).willReturn(false);

            assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

            verify(launcherServiceMock).launchIntegrasjonspunkt("the path");
            verify(mailService).sendMail("Upgrade FAILED test.jar", "theStartupLog");
            verify(kosmosDirectoryRepoMock, never()).blockList(fileMock);
        }
    }

    @Test
    @DisplayName("Apply to null throws exception")
    public void apply_toNull_shouldThrow() {
        assertThrows(NullPointerException.class, () -> target.apply(null));
    }

    @Nested
    @DisplayName("Current version is latest")
    class CurrentIsLatestTests {

        @BeforeEach
        public void setup() {
            given(applicationMock.getCurrent())
                    .willReturn(new ApplicationMetadata().setVersion("latest"));
            given(applicationMock.isSameVersion()).willReturn(true);
        }

        @Test
        @DisplayName("Already running")
        public void apply_currentVersionIsLatestAndHealthStatusIsUp_shouldNotStart() {
            given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.UP);
            assertThat(target.apply(applicationMock)).isSameAs(applicationMock);
            verify(launcherServiceMock, never()).launchIntegrasjonspunkt(anyString());
        }


        @Test
        @DisplayName("Not running, should start up")
        public void apply_currentVersionIsLatestAndHealthStatusIsDown_shouldStart() {
            given(blocklistProperties.isEnabled()).willReturn(true);
            given(propertiesMock.getBlocklist()).willReturn(blocklistProperties);
            given(actuatorServiceMock.getStatus()).willReturn(HealthStatus.DOWN);
            given(fileMock.getAbsolutePath()).willReturn("the path");
            given(applicationMock.getLatest()).willReturn(latestMock);
            given(latestMock.getFile()).willReturn(fileMock);
            given(fileMock.getName()).willReturn("test.jar");
            given(launcherServiceMock.launchIntegrasjonspunkt(any())).willReturn(
                    new LaunchResult()
                            .setStatus(LaunchStatus.SUCCESS)
                            .setStartupLog("theStartupLog")
            );

            assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

            verify(launcherServiceMock).launchIntegrasjonspunkt("the path");
            verify(mailService).sendMail("Upgrade SUCCESS test.jar", "theStartupLog");
        }
    }

    @Test
    @DisplayName("Current version is outdated, update version and launch")
    public void apply_currentVersionIsOldAndHealthStatusIsUp_shouldStart() {
        given(blocklistProperties.isEnabled()).willReturn(true);
        given(propertiesMock.getBlocklist()).willReturn(blocklistProperties);
        given(applicationMock.getCurrent())
                .willReturn(new ApplicationMetadata().setVersion("old"));
        given(applicationMock.isSameVersion()).willReturn(false);
        given(applicationMock.getLatest()).willReturn(latestMock);
        given(fileMock.getAbsolutePath()).willReturn("the path");
        given(latestMock.getFile()).willReturn(fileMock);
        given(fileMock.getName()).willReturn("test.jar");
        given(launcherServiceMock.launchIntegrasjonspunkt(any())).willReturn(
                new LaunchResult()
                        .setStatus(LaunchStatus.SUCCESS)
                        .setStartupLog("theStartupLog")
        );

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(launcherServiceMock).launchIntegrasjonspunkt("the path");
        verify(mailService).sendMail("Upgrade SUCCESS test.jar", "theStartupLog");
    }
}
