package no.difi.move.deploymanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URL;

@Data
@Validated
@ConfigurationProperties(prefix = "deploymanager")
public class DeployManagerProperties {
    @NotNull
    private String root;
    @NotNull
    private URL nexus;
    @NotNull
    private String repository;
    @NotNull
    private String groupId;
    @NotNull
    private String artifactId;
    @NotNull
    private URL shutdownURL;
    @NotNull
    private Integer actuatorConnectTimeoutInMs;
    @NotNull
    private Integer actuatorReadTimeoutInMs;
    @NotNull
    private Integer shutdownRetries;
    @NotNull
    private Integer shutdownPollIntervalInMs;
    @NotNull
    private Integer launchTimeountInMs;
    @NotNull
    private Integer launchPollIntervalInMs;
    @NotNull
    private URL healthURL;
    @NotNull
    private URL nexusProxyURL;
    private String environment = "";
    @NotNull
    private boolean verbose;
    @NotNull
    private String schedulerFixedRateInMs;
    @Valid
    private MailProperties mail;
    @Valid
    private IntegrasjonspunktProperties integrasjonspunkt;
    @Valid
    private KeystoreProperties keystore;

    @Data
    public static class MailProperties {

        @NotNull
        private String recipient;

        @NotNull
        private String from;
    }

    @Data
    public static class IntegrasjonspunktProperties {
        private String profile;
    }
}
