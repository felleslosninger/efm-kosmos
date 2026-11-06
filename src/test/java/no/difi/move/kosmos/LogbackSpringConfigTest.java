package no.difi.move.kosmos;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.CoreConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lastar produksjonsoppsettet for logback (src/main/resources/logback-spring.xml) slik Spring Boot gjer det,
 * og sjekkar at Logstash-appenderen berre blir lagd til når app.logger.enableLogstash=true,
 * og at hendingane får appnamn, versjon, commit, orgnr og server.
 * Spring Boot kastar unntak dersom logback rapporterer feil i oppsettet.
 * Testen endrar den globale LoggerContext, og må derfor ikkje køyre samtidig med andre testar.
 */
@Isolated
class LogbackSpringConfigTest {

    private static final String MAIN_LOGBACK_CONFIG = "file:src/main/resources/logback-spring.xml";
    private static final String MAIN_APPLICATION_PROPERTIES = "src/main/resources/application.properties";

    private final LogbackLoggingSystem loggingSystem = new LogbackLoggingSystem(getClass().getClassLoader());

    @AfterEach
    void restoreLogging() throws Exception {
        loggingSystem.cleanUp();
        LoggerContext loggerContext = loggerContext();
        loggerContext.reset();
        new ContextInitializer(loggerContext).autoConfig();
    }

    @Test
    void logstashAppenderIsAddedWhenEnabled() {
        initialize(environment().withProperty("app.logger.enableLogstash", "true"));

        assertThat(rootLogger().getAppender("stash")).isInstanceOf(LogstashTcpSocketAppender.class);
        assertThat(rootLogger().getAppender("CONSOLE")).isNotNull();
        assertThat(rootLogger().getAppender("FILE")).isNotNull();
    }

    @Test
    void logstashAppenderIsNotAddedWhenDisabled() {
        initialize(environment().withProperty("app.logger.enableLogstash", "false"));

        assertThat(rootLogger().getAppender("stash")).isNull();
        assertThat(rootLogger().getAppender("CONSOLE")).isNotNull();
        assertThat(rootLogger().getAppender("FILE")).isNotNull();
    }

    @Test
    void logstashAppenderIsNotAddedWhenPropertyIsMissing() {
        initialize(environment());

        assertThat(rootLogger().getAppender("stash")).isNull();
        assertThat(rootLogger().getAppender("CONSOLE")).isNotNull();
        assertThat(rootLogger().getAppender("FILE")).isNotNull();
    }

    @Test
    void logstashEventContainsBuildInfoOrgnrAndServer() throws Exception {
        initialize(environment()
                .withProperty("app.logger.enableLogstash", "true")
                .withProperty("project.artifactId", "kosmos")
                .withProperty("project.version", "1.2.3")
                .withProperty("project.buildNumber", "713d918fd58f40917af685be8f3198e97b4bdd8d")
                .withProperty("kosmos.orgnumber", "910077473")
                .withProperty("HOSTNAME", "kosmos-container"));

        JsonNode event = encodeLogstashEvent();

        assertThat(event.path("appname").asText()).isEqualTo("kosmos");
        assertThat(event.path("buildinfo.version").asText()).isEqualTo("1.2.3");
        assertThat(event.path("buildinfo.lastcommit").asText()).isEqualTo("713d918fd58f40917af685be8f3198e97b4bdd8d");
        assertThat(event.path("orgnr").asText()).isEqualTo("910077473");
        assertThat(event.path("server").asText()).isEqualTo("kosmos-container");
    }

    @Test
    void logstashEventUsesLocalHostnameWhenHostnameIsMissing() throws Exception {
        initialize(environment().withProperty("app.logger.enableLogstash", "true"));

        JsonNode event = encodeLogstashEvent();

        assertThat(event.path("server").asText())
                .isNotBlank()
                .doesNotContain("IS_UNDEFINED")
                .isEqualTo(loggerContext().getProperty(CoreConstants.HOSTNAME_KEY));
    }

    @Test
    void loggingStartsWhenOrgnumberIsNotConfigured() throws Exception {
        // Utan difi.move.org.number skal valideringa i KosmosProperties gi feilmeldinga, ikkje logback-oppsettet.
        initialize(environment()
                .withProperty("app.logger.enableLogstash", "true")
                .withProperty("kosmos.orgnumber", mainApplicationProperty("kosmos.orgnumber")));

        assertThat(encodeLogstashEvent().path("orgnr").asText()).isEmpty();
    }

    private static String mainApplicationProperty(String key) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of(MAIN_APPLICATION_PROPERTIES), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties.getProperty(key);
    }

    private static JsonNode encodeLogstashEvent() throws Exception {
        LogstashTcpSocketAppender appender = (LogstashTcpSocketAppender) rootLogger().getAppender("stash");
        LoggingEvent event = new LoggingEvent(Logger.FQCN, rootLogger(), Level.INFO, "Testmelding", null, null);
        return new ObjectMapper().readTree(appender.getEncoder().encode(event));
    }

    private static MockEnvironment environment() {
        return new MockEnvironment().withProperty("app.logger.destination", "localhost:4560");
    }

    private void initialize(MockEnvironment environment) {
        loggingSystem.beforeInitialize();
        loggingSystem.initialize(new LoggingInitializationContext(environment), MAIN_LOGBACK_CONFIG, null);
    }

    private static Logger rootLogger() {
        return loggerContext().getLogger(Logger.ROOT_LOGGER_NAME);
    }

    private static LoggerContext loggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }
}
