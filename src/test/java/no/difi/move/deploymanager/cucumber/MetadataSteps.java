package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class MetadataSteps {

    @Autowired private DeployManagerProperties deployManagerProperties;
    @Autowired private DeployDirectoryRepo deployDirectoryRepo;

    @Given("the metadata.properties contains:")
    public void givenTheMetadataPropertiesContains(String content) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        deployDirectoryRepo.setMetadata(properties);
    }

    @Then("the metadata.properties is updated with:")
    public void thenTheMetadataPropertiesContains(String expectedBody) throws IOException {
        File metaPropertiesFile = new File(deployManagerProperties.getRoot(), "meta.properties");
        List<String> actualLines = Files.readAllLines(metaPropertiesFile.toPath(), StandardCharsets.UTF_8)
                .stream()
                .filter(p -> !p.startsWith("#"))
                .collect(Collectors.toList());

        assertThat(actualLines).containsExactly(expectedBody.split("\\r?\\n"));
    }
}
