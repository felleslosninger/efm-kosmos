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
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Future;

@Service
@Slf4j
@RequiredArgsConstructor
public class LauncherServiceImpl implements LauncherService {

    private final KosmosProperties properties;
    private final ActuatorService actuatorService;
    private final EnvironmentService environmentService;

    @Override
    public LaunchResult launchIntegrasjonspunkt(String jarPath) {
        log.info("Starting application: {}", jarPath);
        return launch(jarPath);
    }

    @SneakyThrows(InterruptedException.class)
    private LaunchResult launch(String jarPath) {
        LaunchResult launchResult = new LaunchResult()
                .setJarPath(jarPath);

        try (StartupLog startupLog = new StartupLog(properties.getIntegrasjonspunkt().isIncludeLog())) {
            log.debug("Starting application in {}", jarPath);

            Future<ProcessResult> future = new ProcessExecutor(Arrays.asList(
                    "java", "-jar", jarPath,
                    "--management.endpoint.shutdown.enabled=true",
                    "--app.logger.enableSSL=false",
                    "--spring.profiles.active=" + properties.getIntegrasjonspunkt().getProfile()))
                    .directory(new File(properties.getIntegrasjonspunkt().getHome()))
                    .environment(environmentService.getChildProcessEnvironment())
                    .redirectOutput(startupLog)
                    .start()
                    .getFuture();

            LaunchStatus launchStatus = waitForStartup(future);
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

    private LaunchStatus waitForStartup(Future<ProcessResult> futureProcessResult) throws InterruptedException {
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
            if (futureProcessResult.isDone() || futureProcessResult.isCancelled()) {
                log.error("Application failed during startup!");
                return LaunchStatus.FAILED;
            }
            if (System.currentTimeMillis() - start >= timeoutInMs) {
                log.error("Application failed to start in " + timeoutInMs + "ms!");
                futureProcessResult.cancel(true);
                return LaunchStatus.FAILED;
            }
        }
    }

}

