package no.difi.move.kosmos.config;

import lombok.Data;

import java.util.List;

@Data
public class EnvironmentProperties {

    private List<String> prefixesRemovedFromChildProcess;

}
