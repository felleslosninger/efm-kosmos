package no.difi.move.deploymanager.config;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.net.URL;

@Data
public class IntegrasjonspunktProperties {

    @NotNull
    @Pattern(regexp = "dev|itest|staging|production")
    private String profile;

    @NotNull
    private URL URL;
    private URL healthURL;
    private URL infoURL;
    private URL shutdownURL;

    @NotNull
    private String latestVersion;

    private String supportedMajorVersion;
}
