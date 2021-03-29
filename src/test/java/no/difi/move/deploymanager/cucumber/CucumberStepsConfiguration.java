package no.difi.move.deploymanager.cucumber;

import com.dumbster.smtp.SimpleSmtpServer;
import com.github.tomakehurst.wiremock.WireMockServer;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.DeployManagerMain;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.IntegrasjonspunktProperties;
import no.difi.move.deploymanager.config.VerificationProperties;
import no.difi.move.deploymanager.service.launcher.LauncherServiceImpl;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = {
        DeployManagerMain.class,
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
    @SpyBean(DeployManagerProperties.class)
    @SpyBean(IntegrasjonspunktProperties.class)
    @SpyBean(VerificationProperties.class)
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
        public IntegrasjonspunktProperties integrasjonspunktProperties(DeployManagerProperties deployManagerProperties) {
            return deployManagerProperties.getIntegrasjonspunkt();
        }

        @Bean
        public VerificationProperties verificationProperties(DeployManagerProperties properties) {
            return properties.getVerification();
        }

        @Bean
        public ContextRefresher contextRefresher() {
            return mock(ContextRefresher.class);
        }
    }

    @Autowired
    private DeployManagerProperties deployManagerProperties;

    @Autowired
    private IntegrasjonspunktProperties integrasjonspunktProperties;

    @Autowired
    private VerificationProperties verificationProperties;

    @Rule
    private final TemporaryFolder temporaryFolder = TemporaryFolder.builder()
            .assureDeletion().build();

    @Before
    @SneakyThrows
    public void before() {
        temporaryFolder.create();

        given(deployManagerProperties.getIntegrasjonspunkt()).willReturn(integrasjonspunktProperties);
        given(integrasjonspunktProperties.getHome()).willReturn(temporaryFolder.getRoot().getAbsolutePath());
        given(deployManagerProperties.getVerification()).willReturn(verificationProperties);
        given(verificationProperties.getPublicKeyPaths()).willReturn(Collections.singletonList(new ClassPathResource("/gpg/public-key.asc").getFile().getAbsolutePath()));
    }

    @After
    public void after() {
        temporaryFolder.delete();
    }
}