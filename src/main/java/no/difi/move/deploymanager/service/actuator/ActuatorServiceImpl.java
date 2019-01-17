package no.difi.move.deploymanager.service.actuator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.HealthStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActuatorServiceImpl implements ActuatorService {

    private final DeployManagerProperties properties;
    private final ActuatorClient actuatorClient;

    public ActuatorServiceImpl(DeployManagerProperties properties, ActuatorClient actuatorClient) {
        this.properties = properties;
        this.actuatorClient = actuatorClient;
    }

    @Override
    public HealthStatus getStatus() {
        log.info("Performing health check.");
        return actuatorClient.getStatus();
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public boolean shutdown() {
        if (!actuatorClient.requestShutdown()) {
            return getStatus() != HealthStatus.UP;
        }

        for (int retries = properties.getShutdownRetries(); retries > 0; --retries) {
            Thread.sleep(properties.getShutdownPollIntervalInMs());

            HealthStatus status = getStatus();
            log.info("Status is {}", status);

            if (status != HealthStatus.UP) {
                return true;
            }
        }

        log.warn("Could not shutdown application");

        return false;
    }
}
