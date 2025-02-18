package no.difi.move.kosmos.cucumber;

import com.dumbster.smtp.SimpleSmtpServer;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.SneakyThrows;
import no.difi.move.kosmos.KosmosMain;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.service.launcher.LauncherServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.BDDMockito.given;

@CucumberContextConfiguration
@ContextConfiguration(classes = {
        KosmosMain.class,
        CucumberStepsConfiguration.SpringConfiguration.class
}, loader = SpringBootContextLoader.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("cucumber")
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

    }

    @Autowired
    private KosmosProperties kosmosProperties;

    @Autowired
    private IntegrasjonspunktProperties integrasjonspunktProperties;

    private Path temporaryPath;

    @Before
    @SneakyThrows
    public void before() {
        temporaryPath = Files.createTempDirectory("temp");
        given(integrasjonspunktProperties.getHome()).willReturn(temporaryPath.toAbsolutePath().toString());
        given(kosmosProperties.getIntegrasjonspunkt()).willReturn(integrasjonspunktProperties);
    }

    @After
    public void after() {
        FileSystemUtils.deleteRecursively(temporaryPath.toFile());
    }

}