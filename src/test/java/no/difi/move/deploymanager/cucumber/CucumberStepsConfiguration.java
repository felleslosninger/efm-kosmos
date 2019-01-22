package no.difi.move.deploymanager.cucumber;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.DeployManagerMain;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.handler.SynchronizationHandler;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.repo.NexusRepo;
import no.difi.move.deploymanager.service.actuator.ActuatorClient;
import no.difi.move.deploymanager.service.laucher.LauncherServiceImpl;
import no.difi.move.deploymanager.service.laucher.dto.LaunchResult;
import no.difi.move.deploymanager.service.laucher.dto.LaunchStatus;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.test.web.client.UnorderedRequestExpectationManager;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ContextConfiguration(classes = {
        DeployManagerMain.class, CucumberStepsConfiguration.SpringConfiguration.class
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
    @Autowired private MockServerRestTemplateCustomizer mockServerRestTemplateCustomizer;
    @Autowired private ActuatorClient actuatorClient;
    @Autowired private DeployDirectoryRepo deployDirectoryRepo;
    @Autowired private NexusRepo nexusRepo;
    @Autowired private DeployManagerProperties propertiesSpy;
    @Autowired private LauncherServiceImpl launcherServiceSpy;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    private ResponseActions responseActions;
    private final ResultCaptor<LaunchResult> launchResultResultCaptor = new ResultCaptor<>(LaunchResult.class);
    private final TemporaryFolder temporaryFolder = new TemporaryFolder();
    private SimpleSmtpServer simpleSmtpServer;

    @Before
    @SneakyThrows
    public void before() {
        simpleSmtpServer = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);
        javaMailSender.setPort(simpleSmtpServer.getPort());

        temporaryFolder.create();
        reset(launcherServiceSpy);
        launchResultResultCaptor.reset();

        doReturn(temporaryFolder.getRoot().getAbsolutePath()).when(propertiesSpy).getRoot();

        mockServerRestTemplateCustomizer.getServers().values().forEach(MockRestServiceServer::reset);
        mockServerRestTemplateCustomizer.customize(actuatorClient.getRestTemplate());

        doAnswer(launchResultResultCaptor).when(launcherServiceSpy).launchIntegrasjonspunkt(any());
    }

    @After
    public void after() {
        simpleSmtpServer.stop();
        temporaryFolder.delete();
    }

    private MockRestServiceServer getServer(String url) {
        return mockServerRestTemplateCustomizer.getServer(getRestTemplate(url));
    }

    @SuppressWarnings("squid:S1172")
    private RestTemplate getRestTemplate(String url) {
        if (url.startsWith("http://localhost:9092")) {
            return actuatorClient.getRestTemplate();
        }

        return nexusRepo.getRestTemplate();
    }

    @Given("the metadata.properties contains:")
    public void givenTheMetadataPropertiesContains(String content) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        deployDirectoryRepo.setMetadata(properties);
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

    @Given("^a \"([^\"]*)\" request to \"([^\"]*)\" will respond with status \"(\\d+)\" and the following \"([^\"]*)\"$")
    public void aRequestToWillRespondWithStatusAndTheFollowing(String method, String url, int statusCode, String contentType, String body) {
        this.responseActions = getServer(url)
                .expect(requestTo(url))
                .andExpect(method(HttpMethod.valueOf(method)));

        responseActions
                .andRespond(withStatus(HttpStatus.valueOf(statusCode))
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(body));
    }

    @And("^a \"([^\"]*)\" request to \"([^\"]*)\" will respond with status \"([^\"]*)\" and the following \"([^\"]*)\" in \"([^\"]*)\"$")
    public void aRequestToWillRespondWithStatusAndTheFollowingIn(String method, String url, int statusCode, String contentType, String path) {
        this.responseActions = getServer(url)
                .expect(requestTo(url))
                .andExpect(method(HttpMethod.valueOf(method)));

        responseActions
                .andRespond(withStatus(HttpStatus.valueOf(statusCode))
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(new ClassPathResource(path)));
    }


    @Given("^a \"([^\"]*)\" request to \"([^\"]*)\" will respond with connection refused$")
    public void aRequestToWillRespondWithNoConnectionRefused(String method, String url) {
        this.responseActions = getServer(url)
                .expect(requestTo(url))
                .andExpect(method(HttpMethod.valueOf(method)));

        responseActions
                .andRespond(request -> {
                    throw new ConnectException("Connection refused: connect");
                });
    }

    @Then("^no JAR is launched$")
    public void noJARIsLaunched() {
        verify(launcherServiceSpy, never()).launchIntegrasjonspunkt(any());
    }

    @Then("^the \"([^\"]*)\" is successfully launched$")
    public void theJARIsSuccessfullyLaunched(String jarName) {
        verify(launcherServiceSpy).launchIntegrasjonspunkt(endsWith(jarName));

        LaunchResult launchResult = launchResultResultCaptor.getLastValue();
        assertThat(launchResult.getStatus()).isSameAs(LaunchStatus.SUCCESS);
    }

    @Then("the metadata.properties is updated with:")
    public void thenTheMetadataPropertiesContains(String expectedBody) throws IOException {
        File metaPropertiesFile = new File(temporaryFolder.getRoot(), "meta.properties");
        List<String> actualLines = Files.readAllLines(metaPropertiesFile.toPath(), StandardCharsets.UTF_8)
                .stream()
                .filter(p -> !p.startsWith("#"))
                .collect(Collectors.toList());

        assertThat(actualLines).containsExactly(expectedBody.split("\\r?\\n"));
    }

    @Then("^an email is sent with subject \"([^\"]*)\" and content:$")
    public void anEmailIsSentWithSubjectAndContent(String subject, String content) {
        assertThat(simpleSmtpServer.getReceivedEmails())
                .extracting(p -> p.getHeaderValue("Subject"), SmtpMessage::getBody)
                .contains(tuple(subject, content.trim()));
    }

    @Then("^no emails are sent$")
    public void noEmailsAreSent() {
        assertThat(simpleSmtpServer.getReceivedEmails()).isEmpty();
    }
}