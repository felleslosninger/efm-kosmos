package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.en.And;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.difi.move.deploymanager.config.DeployManagerProperties;

import java.io.File;
import java.nio.file.Files;

@RequiredArgsConstructor
public class JarSteps {

    private final DeployManagerProperties propertiesSpy;

    @And("^the \"([^\"]*)\" exists$")
    @SneakyThrows
    public void theExists(String jarName) {
        Files.copy(
                getClass().getResourceAsStream("/cucumber/success.jar"),
                new File(propertiesSpy.getRoot(), jarName).toPath()
        );
    }
}
