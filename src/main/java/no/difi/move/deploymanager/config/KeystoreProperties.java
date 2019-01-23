package no.difi.move.deploymanager.config;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class KeystoreProperties {

    @NotNull
    private String path;
    @NotNull
    private String password;
    @NotNull
    private String alias;
}
