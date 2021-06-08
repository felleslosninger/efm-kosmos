package no.difi.move.kosmos.domain.application;

import lombok.Data;
import no.difi.move.kosmos.service.launcher.dto.LaunchResult;

@Data
public class Application {

    private ApplicationMetadata latest;
    private ApplicationMetadata current;
    private LaunchResult launchResult;
    private boolean markedForValidation;

    public boolean isSameVersion() {
        return latest.getVersion().equals(current.getVersion());
    }
}
