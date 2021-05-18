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
public class BlocklistSteps {

    private final DeployManagerProperties propertiesSpy;

    @SneakyThrows
    @Given("^the distribution \"([^\"]*)\" is blocklisted$")
    public void theVersionIsBlocklisted(String version) {
        final Path path = Paths.get(propertiesSpy.getIntegrasjonspunkt().getHome(), version + ".blocklisted");
        if (!new File(path.toString()).createNewFile()) {
            log.error("blocklist file already exists");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(LocalDateTime.now().plusHours(1).toString());
        }
    }

    @SneakyThrows
    @Given("^the distribution \"([^\"]*)\" has an expired blocklist$")
    public void theVersionHasExpiredBlocklist(String version) {
        final Path path = Paths.get(propertiesSpy.getIntegrasjonspunkt().getHome(), version + ".blocklisted");
        if (!new File(path.toString()).createNewFile()) {
            log.error("blocklist file already exists");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(LocalDateTime.now().minusHours(1).toString());
        }
    }
}
