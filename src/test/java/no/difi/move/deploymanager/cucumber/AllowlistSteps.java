package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class AllowlistSteps {

    private final DeployManagerProperties propertiesSpy;

    @SneakyThrows
    @Given("^the distribution \"([^\"]*)\" is allowlisted$")
    public void theVersionIsAllowlisted(String version) {
        final Path path = Paths.get(propertiesSpy.getIntegrasjonspunkt().getHome(), version + ".allowlisted");
        if (!new File(path.toString()).createNewFile()) {
            log.error("Allowlist file already exists");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(LocalDateTime.now().toString());
        }
    }
}
