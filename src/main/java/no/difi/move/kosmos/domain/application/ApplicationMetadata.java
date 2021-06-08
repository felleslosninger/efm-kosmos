package no.difi.move.kosmos.domain.application;

import lombok.Data;

import java.io.File;

@Data
public class ApplicationMetadata {

    private String version;
    private File file;
}
