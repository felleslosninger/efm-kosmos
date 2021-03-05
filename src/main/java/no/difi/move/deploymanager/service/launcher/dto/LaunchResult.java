package no.difi.move.deploymanager.service.launcher.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
public class LaunchResult implements Serializable {

    private String jarPath;
    private LaunchStatus status;
    @ToString.Exclude
    private String startupLog;
}
