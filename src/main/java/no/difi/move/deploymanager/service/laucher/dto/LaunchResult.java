package no.difi.move.deploymanager.service.laucher.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LaunchResult implements Serializable {

    private String jarPath;
    private LaunchStatus status;
    private String startupLog;
}
