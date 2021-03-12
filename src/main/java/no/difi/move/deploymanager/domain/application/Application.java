package no.difi.move.deploymanager.domain.application;

import lombok.Data;
import no.difi.move.deploymanager.service.launcher.dto.LaunchResult;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Data
public class Application {

    private ApplicationMetadata latest;
    private ApplicationMetadata current;
    private ApplicationMetadata signature;
    private LaunchResult launchResult;

    public boolean isSameVersion() {
        return latest.getVersion().equals(current.getVersion());
    }
}
