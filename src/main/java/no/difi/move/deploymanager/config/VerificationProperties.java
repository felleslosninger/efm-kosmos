package no.difi.move.deploymanager.config;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class VerificationProperties {
    @NotNull
    private List<String> publicKeyURLs;
}
