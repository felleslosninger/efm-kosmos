package no.difi.move.kosmos;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lastar produksjonsoppsettet for logback (src/main/resources/logback-spring.xml) slik Spring Boot gjer det,
 * og sjekkar at Logstash-appenderen berre blir lagd til når app.logger.enableLogstash=true.
 * Spring Boot kastar unntak dersom logback rapporterer feil i oppsettet.
 */
class LogbackSpringConfigTest {

    private static final String MAIN_LOGBACK_CONFIG = "file:src/main/resources/logback-spring.xml";

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
