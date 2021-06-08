package no.difi.move.kosmos.config;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.net.URL;

@Data
public class IntegrasjonspunktProperties {

    @NotNull
    private String home;

    @NotNull
    @Pattern(regexp = "dev|itest|staging|production")
    private String profile;

    @NotNull
    private URL baseURL;
    private URL healthURL;
    private URL infoURL;
    private URL shutdownURL;

    private String latestVersion;

    private String supportedMajorVersion;

    private boolean earlyBird;
    private String earlyBirdVersion;

    @NotNull
    private boolean includeLog;
}
