package no.difi.move.kosmos.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class LocalPropertyEnvironmentPostProcessor implements EnvironmentPostProcessor, ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    @SneakyThrows(IOException.class)
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        final FileSystemResource resource = new FileSystemResource("kosmos-local.properties");

        if (resource.exists()) {
            Properties loadAllProperties = PropertiesLoaderUtils.loadProperties(resource);
            environment.getPropertySources().addFirst(new PropertiesPropertySource("file:kosmos-local.properties", loadAllProperties));
            log.info("Added {}", resource.getFile().getAbsolutePath());
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent e) {
        this.postProcessEnvironment(e.getEnvironment(), e.getSpringApplication());
    }
}
