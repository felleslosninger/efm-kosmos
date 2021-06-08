package no.difi.move.kosmos.cucumber;

import com.dumbster.smtp.SimpleSmtpServer;
import com.github.tomakehurst.wiremock.WireMockServer;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.KosmosMain;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.service.launcher.LauncherServiceImpl;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = {
        KosmosMain.class,
        CucumberStepsConfiguration.SpringConfiguration.class
}, loader = SpringBootContextLoader.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("cucumber")
@Slf4j
public class CucumberStepsConfiguration {

    @Configuration
    @Profile("cucumber")
    @SpyBean(KosmosProperties.class)
    @SpyBean(IntegrasjonspunktProperties.class)
    @SpyBean(LauncherServiceImpl.class)
    public static class SpringConfiguration {

        @Bean
        public WireMockServer wireMockServer() {
            return new WireMockServer(options().port(9092));
        }

        @Bean
        public WireMockMonitor wireMockMonitor(WireMockServer wireMockServer) {
            return new WireMockMonitor(wireMockServer);
        }

        @Bean
        public CucumberResourceLoader cucumberResourceLoader() {
            return new CucumberResourceLoader();
        }

        @Bean
        @SneakyThrows
        public SimpleSmtpServer simpleSmtpServer() {
            return SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);
        }

        @Bean
        public IntegrasjonspunktProperties integrasjonspunktProperties(KosmosProperties kosmosProperties) {
            return kosmosProperties.getIntegrasjonspunkt();
        }

        @Bean
        public ContextRefresher contextRefresher() {
            return mock(ContextRefresher.class);
        }
    }

    @Autowired
    private KosmosProperties kosmosProperties;

    @Autowired
    private IntegrasjonspunktProperties integrasjonspunktProperties;

    @Rule
    private final TemporaryFolder temporaryFolder = TemporaryFolder.builder()
            .assureDeletion().build();

    @Before
    @SneakyThrows
    public void before() {
        temporaryFolder.create();
        given(kosmosProperties.getIntegrasjonspunkt()).willReturn(integrasjonspunktProperties);
        given(integrasjonspunktProperties.getHome()).willReturn(temporaryFolder.getRoot().getAbsolutePath());
    }

    @After
    public void after() {
        temporaryFolder.delete();
    }
}