package no.difi.move.kosmos.service.launcher;

import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.service.launcher.dto.LaunchResult;
import no.difi.move.kosmos.service.launcher.dto.LaunchStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class LauncherServiceImplTest {

    private static final String TEST_APP_SOURCE = """
            public class TestApp {
                public static void main(String[] args) throws Exception {
                    System.out.println("Started TestApp in " + System.getProperty("user.dir"));
                    System.out.println("KOSMOS_TEST_VARIABLE=" + System.getenv("KOSMOS_TEST_VARIABLE"));
                    System.out.println("ARGS=" + String.join(" ", args));
                    System.err.println("Line written to stderr");
                    String sleep = System.getenv("KOSMOS_TEST_SLEEP_MS");
                    if (sleep != null) {
                        Thread.sleep(Long.parseLong(sleep));
                    }
                }
            }
            """;

    @TempDir
    static Path tempDir;
    private static Path testAppJar;

    @Mock
    private KosmosProperties properties;
    @Mock
    private ActuatorService actuatorService;
    @Mock
    private EnvironmentService environmentService;

    @InjectMocks
    private LauncherServiceImpl launcherService;

    private final Map<String, String> childEnvironment = new HashMap<>();

    @BeforeAll
    static void buildTestApp() throws IOException {
        Path sourceDir = Files.createDirectories(tempDir.resolve("src"));
        Path source = Files.writeString(sourceDir.resolve("TestApp.java"), TEST_APP_SOURCE);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler.run(null, null, null, "-d", sourceDir.toString(), source.toString())).isZero();

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "TestApp");
        testAppJar = tempDir.resolve("testapp.jar");
        try (OutputStream out = Files.newOutputStream(testAppJar);
             JarOutputStream jar = new JarOutputStream(out, manifest)) {
            jar.putNextEntry(new JarEntry("TestApp.class"));
            jar.write(Files.readAllBytes(sourceDir.resolve("TestApp.class")));
            jar.closeEntry();
        }
    }

    @BeforeEach
    void before() {
        lenient().when(properties.getIntegrasjonspunkt()).thenReturn(
                new IntegrasjonspunktProperties()
                        .setProfile("staging")
                        .setHome(tempDir.toString())
        );
        lenient().when(properties.getLaunchPollIntervalInMs()).thenReturn(100);
        lenient().when(properties.getLaunchTimeoutInMs()).thenReturn(10_000);
        lenient().when(environmentService.getChildProcessEnvironment()).thenReturn(childEnvironment);
    }

    @AfterEach
    void killChildProcesses() {
        ProcessHandle.current().descendants().forEach(ProcessHandle::destroyForcibly);
    }

    @Test
    void launchIntegrasjonspunkt_whenHealthIsUp_thenSuccessAndProcessKeepsRunning() {
        childEnvironment.put("KOSMOS_TEST_SLEEP_MS", "30000");
        given(actuatorService.getStatus()).willReturn(HealthStatus.UP);

        LaunchResult result = launcherService.launchIntegrasjonspunkt(testAppJar.toString());

        assertThat(result)
                .hasFieldOrPropertyWithValue("jarPath", testAppJar.toString())
                .hasFieldOrPropertyWithValue("status", LaunchStatus.SUCCESS);
        assertThat(ProcessHandle.current().children().anyMatch(ProcessHandle::isAlive)).isTrue();
    }

    @Test
    void launchIntegrasjonspunkt_whenProcessExits_thenFailedWithCompleteStartupLog() {
        childEnvironment.put("KOSMOS_TEST_VARIABLE", "from-kosmos");
        given(actuatorService.getStatus()).willReturn(HealthStatus.UNKNOWN);

        LaunchResult result = launcherService.launchIntegrasjonspunkt(testAppJar.toString());

        assertThat(result.getStatus()).isEqualTo(LaunchStatus.FAILED);
        assertThat(result.getStartupLog())
                .contains("Started TestApp in " + tempDir)
                .contains("KOSMOS_TEST_VARIABLE=from-kosmos")
                .contains("ARGS=--management.endpoint.shutdown.enabled=true --app.logger.enableSSL=false --spring.profiles.active=staging")
                .contains("Line written to stderr");
    }

    @Test
    void launchIntegrasjonspunkt_whenStartupTimesOut_thenFailedAndProcessIsStopped() {
        childEnvironment.put("KOSMOS_TEST_SLEEP_MS", "30000");
        given(properties.getLaunchTimeoutInMs()).willReturn(500);
        given(actuatorService.getStatus()).willReturn(HealthStatus.UNKNOWN);

        LaunchResult result = launcherService.launchIntegrasjonspunkt(testAppJar.toString());

        assertThat(result.getStatus()).isEqualTo(LaunchStatus.FAILED);
        await().atMost(Duration.ofSeconds(10))
                .until(() -> ProcessHandle.current().children().noneMatch(ProcessHandle::isAlive));
    }

    @Test
    void launchIntegrasjonspunkt_whenProcessCannotStart_thenFailed() {
        given(properties.getIntegrasjonspunkt()).willReturn(
                new IntegrasjonspunktProperties()
                        .setProfile("staging")
                        .setHome(tempDir.resolve("does-not-exist").toString())
        );

        LaunchResult result = launcherService.launchIntegrasjonspunkt(testAppJar.toString());

        assertThat(result.getStatus()).isEqualTo(LaunchStatus.FAILED);
        assertThat(result.getStartupLog()).contains("Cannot run program");
    }

    @Test
    void applyEnvironment_putsValuesAndRemovesNullValues() {
        Map<String, String> target = new HashMap<>(Map.of("KEEP", "1", "REMOVE", "2", "REPLACE", "3"));
        Map<String, String> variables = new HashMap<>();
        variables.put("REMOVE", null);
        variables.put("REPLACE", "new");
        variables.put("ADD", "4");

        LauncherServiceImpl.applyEnvironment(target, variables);

        assertThat(target).containsExactlyInAnyOrderEntriesOf(Map.of("KEEP", "1", "REPLACE", "new", "ADD", "4"));
    }

}
