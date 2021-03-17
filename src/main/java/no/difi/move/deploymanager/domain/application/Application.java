package no.difi.move.deploymanager.domain.application;

import lombok.Data;
import no.difi.move.deploymanager.service.launcher.dto.LaunchResult;

@Data
public class Application {

    private ApplicationMetadata latest;
    private ApplicationMetadata current;
    private LaunchResult launchResult;

    public boolean isSameVersion() {
        return latest.getVersion().equals(current.getVersion());
    }
}
