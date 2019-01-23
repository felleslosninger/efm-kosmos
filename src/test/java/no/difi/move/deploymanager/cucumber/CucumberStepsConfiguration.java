package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.DeployManagerMain;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.handler.SynchronizationHandler;
import no.difi.move.deploymanager.service.laucher.LauncherServiceImpl;
import org.junit.After;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.UnorderedRequestExpectationManager;

import java.io.File;
import java.nio.file.Files;

import static org.mockito.Mockito.doReturn;

@ContextConfiguration(classes = {
        DeployManagerMain.class,
        CucumberStepsConfiguration.SpringConfiguration.class
}, loader = SpringBootContextLoader.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("cucumber")
@AutoConfigureWebClient(registerRestTemplate = true)
@Slf4j
public class CucumberStepsConfiguration {

    @Configuration
    @Profile("cucumber")
    @SpyBean(DeployManagerProperties.class)
    @SpyBean(LauncherServiceImpl.class)
    public static class SpringConfiguration {

        @Bean
        public MockServerRestTemplateCustomizer mockServerRestTemplateCustomizer() {
            return new MockServerRestTemplateCustomizer(UnorderedRequestExpectationManager.class);
        }
    }

    @Autowired private SynchronizationHandler synchronizationHandler;
    @Autowired private DeployManagerProperties propertiesSpy;

    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    @SneakyThrows
    public void before() {
        temporaryFolder.create();
        doReturn(temporaryFolder.getRoot().getAbsolutePath()).when(propertiesSpy).getRoot();
    }

    @After
    public void after() {
        temporaryFolder.delete();
    }


    @And("^the \"([^\"]*)\" exists$")
    @SneakyThrows
    public void theExists(String jarName) {
        Files.copy(
                getClass().getResourceAsStream("/cucumber/success.jar"),
                new File(temporaryFolder.getRoot(), jarName).toPath()
        );
    }

    @Given("the synchronization handler is triggered")
    public void theSynchronizationHandlerIsTriggered() {
        synchronizationHandler.run();
    }
}