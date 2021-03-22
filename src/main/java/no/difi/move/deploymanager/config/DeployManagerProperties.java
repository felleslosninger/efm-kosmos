package no.difi.move.deploymanager.config;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Mod11Check;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.net.URL;

@Data
@Validated
@ConfigurationProperties(prefix = "deploymanager")
public class DeployManagerProperties {

    /**
     * Organization number to run as.
     */
    @NotNull(message = "deploymanager.orgnumber is not set. This property is required.")
    @Digits(integer = 9, fraction = 0, message = "deploymanager.orgnumber must be exactly 9 digits")
    @Length(min = 9, max = 9, message = "deploymanager.orgnumber must be exactly 9 digits")
    @Mod11Check(threshold = 7, message = "deploymanager.orgnumber has wrong control character")
    private String orgnumber;

    @NotNull
    private URL nexus;

    @NotNull
    @Pattern(regexp = "itest|staging|releases")
    private String repository;

    @NotNull
    private String groupId;

    @NotNull
    private String artifactId;

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
    private Integer launchTimeoutInMs;

    @NotNull
    @Positive
    private Integer launchPollIntervalInMs;

    @NotNull
    @Positive
    private Integer nexusConnectTimeoutInMs;

    @NotNull
    @Positive
    private Integer nexusReadTimeoutInMs;

    @NotNull
    @Max(120000L)
    @Positive
    private Integer schedulerFixedRateInMs;

    @Valid
    @NestedConfigurationProperty
    private BlacklistProperties blacklist;

    @Valid
    @NestedConfigurationProperty
    private IntegrasjonspunktProperties integrasjonspunkt;

    @Valid
    @NestedConfigurationProperty
    private VerificationProperties verification;

    @Valid
    @NestedConfigurationProperty
    private MailProperties mail;

    @Valid
    @NestedConfigurationProperty
    private KeystoreProperties keystore;

    @NestedConfigurationProperty
    private EnvironmentProperties environment;

}
