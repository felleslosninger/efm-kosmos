package no.difi.move.kosmos.config;

import lombok.Data;
import org.springframework.core.io.Resource;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class VerificationProperties {
    @NotNull
    private List<Resource> publicKeyPaths;
}
