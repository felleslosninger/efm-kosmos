package no.difi.move.deploymanager.config;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class VerificationProperties {
    @NotNull
    private String publicKeyURL;
}
