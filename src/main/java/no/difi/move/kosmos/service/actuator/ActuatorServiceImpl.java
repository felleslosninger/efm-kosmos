package no.difi.move.kosmos.service.actuator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.domain.VersionInfo;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActuatorServiceImpl implements ActuatorService {

    private final KosmosProperties properties;
    private final ActuatorClient actuatorClient;

    public ActuatorServiceImpl(KosmosProperties properties, ActuatorClient actuatorClient) {
        this.properties = properties;
        this.actuatorClient = actuatorClient;
    }

    @Override
    public HealthStatus getStatus() {
        log.debug("Performing health check");
        return actuatorClient.getStatus();
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public boolean shutdown() {
        log.trace("Calling ActuatorServiceImpl.shutdown()");
        if (!actuatorClient.requestShutdown()) {
            return getStatus() != HealthStatus.UP;
        }

        int shutdownRetries = properties.getShutdownRetries();
        int pollIntervalInMs = properties.getShutdownPollIntervalInMs();
        log.debug("Retries shutdown {} times with interval {}", shutdownRetries, pollIntervalInMs);
        for (int retries = shutdownRetries; retries > 0; --retries) {
            Thread.sleep(pollIntervalInMs);

            HealthStatus status = getStatus();
            log.info("Health status is {}", status);

            if (status != HealthStatus.UP) {
                return true;
            }
        }

        log.warn("Could not shutdown application");

        return false;
    }

    @Override
    public VersionInfo getVersionInfo() {
        return actuatorClient.getVersionInfo();
    }
}
