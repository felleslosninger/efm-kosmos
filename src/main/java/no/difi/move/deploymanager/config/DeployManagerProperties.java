package no.difi.move.deploymanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
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
    @Pattern(regexp = "staging|releases")
    private String repository;

    @NotNull
    private String groupId;

    @NotNull
    private String artifactId;

    @NotNull
    private URL shutdownURL;

    @NotNull
    @Positive
    private Integer actuatorConnectTimeoutInMs;

    @NotNull
    @Positive
    private Integer actuatorReadTimeoutInMs;

    @NotNull
    @Positive
    private Integer shutdownRetries;

    @NotNull
    @Positive
    private Integer shutdownPollIntervalInMs;

    @NotNull
    @Positive
    private Integer launchTimeountInMs;

    @NotNull
    @Positive
    private Integer launchPollIntervalInMs;

    @NotNull
    private URL healthURL;

    @NotNull
    private URL nexusProxyURL;

    private String environment = "";

    @NotNull
    private boolean verbose;

    @NotNull
    @Max(120000L)
    @Positive
    private Integer schedulerFixedRateInMs;

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private IntegrasjonspunktProperties integrasjonspunkt;

    @Valid
    @NestedConfigurationProperty
    private MailProperties mail;

    @Valid
    @NestedConfigurationProperty
    private KeystoreProperties keystore;
}
