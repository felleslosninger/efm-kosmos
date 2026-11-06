package no.difi.move.kosmos.service.launcher;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.service.launcher.dto.LaunchResult;
import no.difi.move.kosmos.service.launcher.dto.LaunchStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LauncherServiceImpl implements LauncherService {

    private static final Duration OUTPUT_DRAIN_TIMEOUT = Duration.ofSeconds(1);

    private final KosmosProperties properties;
    private final ActuatorService actuatorService;
    private final EnvironmentService environmentService;

    private Process process;

    @Override
    public LaunchResult launchIntegrasjonspunkt(String jarPath) {
        log.info("Starting application: {}", jarPath);
        return launch(jarPath);
    }

    @SneakyThrows(InterruptedException.class)
    private LaunchResult launch(String jarPath) {

        if ((process != null) && process.isAlive()) {
            log.warn("Previous application had not completely shut down yet, this might prevent new version to start when using H2 database.");
        }

        LaunchResult launchResult = new LaunchResult()
                .setJarPath(jarPath);

        StartupLog startupLog = new StartupLog(properties.getIntegrasjonspunkt().isIncludeLog());
        try {
            log.debug("Starting application in {}", jarPath);

            ProcessBuilder processBuilder = new ProcessBuilder(List.of(
                    "java", "-jar", jarPath,
                    "--management.endpoint.shutdown.enabled=true",
                    "--app.logger.enableSSL=false",
                    "--spring.profiles.active=" + properties.getIntegrasjonspunkt().getProfile()))
                    .directory(new File(properties.getIntegrasjonspunkt().getHome()))
                    .redirectErrorStream(true);
            applyEnvironment(processBuilder.environment(), environmentService.getChildProcessEnvironment());

            process = processBuilder.start();
            Thread outputReader = Thread.ofPlatform()
                    .name("integrasjonspunkt-output")
                    .daemon(true)
                    .start(readOutput(process, startupLog));

            LaunchStatus launchStatus = waitForStartup(process);
            if (!process.isAlive()) {
                // the process has ended, make sure the startup log contains all of its output
                outputReader.join(OUTPUT_DRAIN_TIMEOUT);
            }
            startupLog.stopRecording();
            launchResult
                    .setStatus(launchStatus)
                    .setStartupLog(startupLog.getLog());
        } catch (IOException e) {
            log.error("Failed to launch process", e);
            launchResult
                    .setStatus(LaunchStatus.FAILED)
                    .setStartupLog(e.getLocalizedMessage());
        }

        return launchResult;
    }

    private static Runnable readOutput(Process process, StartupLog startupLog) {
        return () -> startupLog.consume(process.getInputStream());
    }

    /**
     * Applies the given variables to the child process environment. A {@code null} value removes the variable.
     */
    static void applyEnvironment(Map<String, String> target, Map<String, String> variables) {
        variables.forEach((name, value) -> {
            if (value == null) {
                target.remove(name);
            } else {
                target.put(name, value);
            }
        });
    }

    private LaunchStatus waitForStartup(Process startedProcess) throws InterruptedException {
        int pollIntervalInMs = properties.getLaunchPollIntervalInMs();
        int timeoutInMs = properties.getLaunchTimeoutInMs();
        log.debug("Waiting {} ms for startup with timeout after {}", pollIntervalInMs, timeoutInMs);
        long start = System.currentTimeMillis();

        while (true) {
            log.info("Waiting for health check to pass");
            Thread.sleep(pollIntervalInMs);
            if (actuatorService.getStatus() == HealthStatus.UP) {
                log.info("Application started successfully!");
                return LaunchStatus.SUCCESS;
            }
            if (!startedProcess.isAlive()) {
                log.error("Application failed during startup!");
                return LaunchStatus.FAILED;
            }
            if (System.currentTimeMillis() - start >= timeoutInMs) {
                log.error("Application failed to start in " + timeoutInMs + "ms!");
                startedProcess.destroy();
                return LaunchStatus.FAILED;
            }
        }
    }

}
