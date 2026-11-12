package no.difi.move.kosmos.service.config;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class RefreshServiceImplTest {

    private MockWebServer mockWebServer;
    private RefreshServiceImpl target;

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        target = new RefreshServiceImpl();
        ReflectionTestUtils.setField(target, "url", mockWebServer.url("/latest-versions.yml").toString());
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Test
    public void refreshConfig_ValidYaml_ShouldReturnConfig() throws IOException {
        enqueueValidYaml();
        assertThat(latestStaging(target.refreshConfig())).isEqualTo("3.0.0");
    }

    @Test
    public void refreshConfig_ServerErrorAfterValidConfig_ShouldReturnLastValidConfig() throws IOException {
        enqueueValidYaml();
        var first = target.refreshConfig();
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        assertThat(target.refreshConfig()).isSameAs(first);
    }

    @Test
    public void refreshConfig_ServerUnavailableAfterValidConfig_ShouldReturnLastValidConfig() throws IOException {
        enqueueValidYaml();
        var first = target.refreshConfig();
        mockWebServer.close();
        assertThat(target.refreshConfig()).isSameAs(first);
    }

    @Test
    public void refreshConfig_InvalidYamlAfterValidConfig_ShouldReturnLastValidConfig() throws IOException {
        enqueueValidYaml();
        var first = target.refreshConfig();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("integrasjonspunkt: [ not valid"));
        assertThat(target.refreshConfig()).isSameAs(first);
    }

    @Test
    public void refreshConfig_ServerErrorWithoutValidConfig_ShouldReturnNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        assertThat(target.refreshConfig()).isNull();
    }

    private void enqueueValidYaml() throws IOException {
        try (var yaml = RefreshServiceImplTest.class.getResourceAsStream("/versions/latest-versions.yml")) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(new String(yaml.readAllBytes())));
        }
    }

    private static String latestStaging(VersionsConfig config) {
        return config.integrasjonspunkt().environments().get("staging").latest();
    }

}
