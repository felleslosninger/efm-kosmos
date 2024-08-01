package no.difi.move.kosmos.service.actuator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.HealthStatus;
import no.difi.move.kosmos.service.actuator.dto.HealthResource;
import no.difi.move.kosmos.service.actuator.dto.InfoResource;
import no.difi.move.kosmos.service.actuator.dto.ShutdownResource;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebClientActuatorClientTest {

    private static MockWebServer mockWebServer;
    private static final IntegrasjonspunktProperties INTEGRASJONSPUNKT_PROPERTIES = mock(IntegrasjonspunktProperties.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private KosmosProperties properties;

    @InjectMocks
    private WebClientActuatorClient target;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        when(INTEGRASJONSPUNKT_PROPERTIES.getHealthURL())
                .thenReturn(mockWebServer.url("/health").url());
        when(INTEGRASJONSPUNKT_PROPERTIES.getShutdownURL())
                .thenReturn(mockWebServer.url("/shutdown").url());
        when(INTEGRASJONSPUNKT_PROPERTIES.getInfoURL())
                .thenReturn(mockWebServer.url("/info").url());
        when(properties.getIntegrasjonspunkt()).thenReturn(INTEGRASJONSPUNKT_PROPERTIES);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockWebServer.close();
    }

    @SneakyThrows
    @Test
    public void getHealthStatus_StatusIsUp_ShouldReturnHealthStatusUp() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(json(new HealthResource().setStatus("UP"))));
        assertThat(target.getStatus()).isSameAs(HealthStatus.UP);
    }

    @Test
    public void getHealthStatus_BadRequest_ShouldReturnHealthStatusUnknown() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        assertThat(target.getStatus()).isSameAs(HealthStatus.UNKNOWN);
    }

    @Test
    public void getHealthStatus_InternalServerError_ShouldReturnHealthStatusUnknown() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        assertThat(target.getStatus()).isSameAs(HealthStatus.UNKNOWN);
    }

    @Test
    public void requestShutdown_Success_ShouldReturnTrue() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(json(new ShutdownResource().setMessage("Shutting down, bye..."))));
        assertThat(target.requestShutdown()).isTrue();
    }

    @Test
    public void requestShutdown_BadRequest_ShouldReturnFalse() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        assertThat(target.requestShutdown()).isFalse();
    }

    @Test
    public void requestShutdown_InternalServerError_ShouldReturnFalse() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        assertThat(target.requestShutdown()).isFalse();
    }

    @Test
    public void getVersion_ValidResponse_ShouldReturnResolvedVersion() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(json(new InfoResource().setBuild
                        (new InfoResource.BuildResource().setVersion("1")))));
        assertThat(StringUtils.equals("1", target.getVersionInfo().getVersion())).isTrue();
    }

    @Test
    public void getVersion_BadRequest_ShouldReturnUnresolved() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        assertThat(target.getVersionInfo().isResolved()).isFalse();
    }

    @Test
    public void getVersion_InternalServerError_ShouldReturnUnresolved() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        assertThat(target.getVersionInfo().isResolved()).isFalse();
    }

    @SneakyThrows(JsonProcessingException.class)
    private String json(Object value) {
        return OBJECT_MAPPER.writeValueAsString(value);
    }

}