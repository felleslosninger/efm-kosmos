package no.difi.move.kosmos.cucumber;

import cucumber.api.java.en.And;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.config.KosmosProperties;

import java.io.File;
import java.nio.file.Files;

@Slf4j
@RequiredArgsConstructor
public class JarSteps {

    private final KosmosProperties propertiesSpy;

    @And("^the \"([^\"]*)\" exists as a copy of \"([^\"]*)\"$")
    @SneakyThrows
    public void theExists(String path, String copyOf) {
        Files.copy(
                getClass().getResourceAsStream(copyOf),
                new File(propertiesSpy.getIntegrasjonspunkt().getHome(), path).toPath()
        );
    }

    @And("^the \"([^\"]*)\" exists$")
    @SneakyThrows
    public void theExists(String path) {
        if (!new File(propertiesSpy.getIntegrasjonspunkt().getHome(), path).createNewFile()) {
            log.error("File already exists!");
        }
    }
}
