package no.difi.move.deploymanager.domain.application;

import lombok.Data;

import java.io.File;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Data
public class ApplicationMetadata {

    private String version;
    private File file;
}
