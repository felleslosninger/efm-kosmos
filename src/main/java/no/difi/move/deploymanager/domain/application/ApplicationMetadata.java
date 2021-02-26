package no.difi.move.deploymanager.domain.application;

import lombok.Data;

import java.io.File;

@Data
public class ApplicationMetadata {

    private String version;
    private File file;
}
