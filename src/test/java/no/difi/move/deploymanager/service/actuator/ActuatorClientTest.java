package no.difi.move.deploymanager.service.actuator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.difi.move.deploymanager.command.Command;
import no.difi.move.deploymanager.domain.HealthStatus;
import no.difi.move.deploymanager.service.actuator.dto.HealthResource;
import no.difi.move.deploymanager.service.actuator.dto.ShutdownResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RunWith(SpringRunner.class)
@RestClientTest(ActuatorClient.class)
public class ActuatorClientTest {

    private static final String HEALTH_URI = "http://localhost:8080/manage/health";
    private static final String SHUTDOWN_URI = "http://localhost:8080/manage/shutdown";

    @Autowired private ActuatorClient client;
    @Autowired private MockRestServiceServer server;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private Command command;

    @Test
    public void testGetStatus() {
        server.expect(requestTo(HEALTH_URI))
                .andRespond(withSuccess(json(
                        new HealthResource()
                                .setStatus("UP")
                ), MediaType.APPLICATION_JSON));

        assertThat(client.getStatus()).isSameAs(HealthStatus.UP);
    }

    @Test
    public void testGetStatusWhenBadRequest() {
        server.expect(requestTo(HEALTH_URI))
                .andRespond(withBadRequest());

        assertThat(client.getStatus()).isSameAs(HealthStatus.UNKOWN);
    }

    @Test
    public void testGetStatusWhenServerError() {
        server.expect(requestTo(HEALTH_URI))
                .andRespond(withServerError());

        assertThat(client.getStatus()).isSameAs(HealthStatus.UNKOWN);
    }

    @Test
    public void testGetStatusWhenResourceAccessException() {
        server.expect(requestTo(HEALTH_URI))
                .andRespond((response) -> {
                    throw new ResourceAccessException("Connection failed");
                });

        assertThat(client.getStatus()).isSameAs(HealthStatus.UNKOWN);
    }

    @Test
    public void testRequestShutdown() {
        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(SHUTDOWN_URI))
                .andRespond(withSuccess(json(
                        new ShutdownResource()
                                .setMessage("Shutting down, bye...")
                ), MediaType.APPLICATION_JSON));

        assertThat(client.requestShutdown()).isTrue();
    }

    @Test
    public void testRequestShutdownWhenBadRequest() {
        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(SHUTDOWN_URI))
                .andRespond(withBadRequest());

        assertThat(client.requestShutdown()).isFalse();
    }

    @Test
    public void testRequestShutdownWhenServerError() {
        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(SHUTDOWN_URI))
                .andRespond(withServerError());

        assertThat(client.requestShutdown()).isFalse();
    }

    @Test
    public void testRequestShutdownWhenResourceAccessException() {
        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(SHUTDOWN_URI))
                .andRespond((response) -> {
                    throw new ResourceAccessException("Connection failed");
                });

        assertThat(client.requestShutdown()).isFalse();
    }

    @SneakyThrows(JsonProcessingException.class)
    private String json(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
