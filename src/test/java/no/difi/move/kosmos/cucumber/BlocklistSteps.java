package no.difi.move.kosmos.cucumber;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.config.KosmosProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class BlocklistSteps {

    private final KosmosProperties propertiesSpy;

    @SneakyThrows
    @Given("^the distribution \"([^\"]*)\" is blocklisted$")
    public void theVersionIsBlocklisted(String version) {
        final Path path = Path.of(propertiesSpy.getIntegrasjonspunkt().getHome(), version + ".blocklisted");
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
        final Path path = Path.of(propertiesSpy.getIntegrasjonspunkt().getHome(), version + ".blocklisted");
        if (!new File(path.toString()).createNewFile()) {
            log.error("blocklist file already exists");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(LocalDateTime.now().minusHours(1).toString());
        }
    }
}
