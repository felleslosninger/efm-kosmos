package no.difi.move.deploymanager.config;

import lombok.Data;

import java.util.List;

@Data
public class EnvironmentProperties {

    private List<String> prefixesRemovedFromChildProcess;

}
