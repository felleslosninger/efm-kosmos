package no.difi.move.kosmos.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

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

    private boolean earlyBird;
    private String supportedMajorVersion;

    @NotNull
    private boolean includeLog;

}
