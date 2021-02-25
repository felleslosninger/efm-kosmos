package no.difi.move.deploymanager.cucumber;

import com.dumbster.smtp.SimpleSmtpServer;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.DeployManagerMain;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.IntegrasjonspunktProperties;
import no.difi.move.deploymanager.service.launcher.LauncherServiceImpl;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.UnorderedRequestExpectationManager;

import static org.mockito.Mockito.*;

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
        @SneakyThrows
        public SimpleSmtpServer simpleSmtpServer() {
            return SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);
        }

        @Bean
        public MockServerRestTemplateCustomizer mockServerRestTemplateCustomizer() {
            return new MockServerRestTemplateCustomizer(UnorderedRequestExpectationManager.class);
        }

        @Bean
        public IntegrasjonspunktProperties mockIntegrasjonspunktProperties() {
            return mock(IntegrasjonspunktProperties.class);
        }

        @Bean
        public ContextRefresher contextRefresher(){
            return mock(ContextRefresher.class);
        }

    }

    @Autowired
    private DeployManagerProperties propertiesSpy;

    @Autowired
    private IntegrasjonspunktProperties integrasjonspunktPropertiesMock;

    @Rule
    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    @SneakyThrows
    public void before() {
        temporaryFolder.create();
        doReturn(temporaryFolder.getRoot().getAbsolutePath()).when(integrasjonspunktPropertiesMock).getHome();
        when(propertiesSpy.getIntegrasjonspunkt()).thenReturn(integrasjonspunktPropertiesMock);
    }

    @After
    public void after() {
        temporaryFolder.delete();
    }
}