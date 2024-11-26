package no.difi.move.kosmos.cucumber;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RequiredArgsConstructor
public class RestSteps {

    public static final String TEST_SCENARIO = "Test scenario";
    private final CucumberResourceLoader cucumberResourceLoader;
    private final WireMockServer wireMockServer;

    @After
    public void after() {
        wireMockServer.resetAll();
        wireMockServer.resetScenarios();
    }

    @Given("^a \"([^\"]*)\" request to \"([^\"]*)\" will respond with status \"(\\d+)\" and the following \"([^\"]*)\"$")
    public void aRequestToWillRespondWithStatusAndTheFollowing(String method, String url, int statusCode, String contentType, String body) {
        wireMockServer.stubFor(request(method, urlEqualTo(url))
                .inScenario(TEST_SCENARIO)
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader(HttpHeaders.CONTENT_TYPE, contentType)
                        .withBody(body)));
    }

    @And("^a \"([^\"]*)\" request to \"([^\"]*)\" will respond with status \"([^\"]*)\" and the following \"([^\"]*)\" in \"([^\"]*)\"$")
    public void aRequestToWillRespondWithStatusAndTheFollowingIn(String method, String url, int statusCode, String contentType, String path) throws IOException {
        wireMockServer.stubFor(request(method, urlEqualTo(url))
                .inScenario(TEST_SCENARIO)
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader(HttpHeaders.CONTENT_TYPE, contentType)
                        .withBody(cucumberResourceLoader.getResourceBytes(path))));
    }


    @Given("^state is \"([^\"]*)\" then a \"([^\"]*)\" request to \"([^\"]*)\" will set the state to \"([^\"]*)\" and respond with connection refused$")
    public void aRequestToWillRespondWithNoConnectionRefused(String scenarioState,  String method, String url, String newState) {
        wireMockServer.stubFor(request(method, urlEqualTo(url))
                .inScenario(TEST_SCENARIO)
                .whenScenarioStateIs(scenarioState)
                .willSetStateTo(newState)
                .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
    }

    @Given("^state is \"([^\"]*)\" then a \"([^\"]*)\" request to \"([^\"]*)\" will set the state to \"([^\"]*)\" and respond with status \"(\\d+)\" and the following \"([^\"]*)\"$")
    public void aRequestToWillRespondWithStatusAndTheFollowing(String scenarioState, String method, String url, String newState, int statusCode, String contentType, String body) {
        wireMockServer.stubFor(request(method, urlEqualTo(url))
                .inScenario(TEST_SCENARIO)
                .whenScenarioStateIs(scenarioState)
                .willSetStateTo(newState)
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader(HttpHeaders.CONTENT_TYPE, contentType)
                        .withBody(body)));
    }

    @Given("^state is \"([^\"]*)\" then a \"([^\"]*)\" request to \"([^\"]*)\" will respond with status \"(\\d+)\" and the following \"([^\"]*)\"$")
    public void aRequestToWillRespondWithStatusAndTheFollowing(String scenarioState, String method, String url, int statusCode, String contentType, String body) {
        wireMockServer.stubFor(request(method, urlEqualTo(url))
                .inScenario(TEST_SCENARIO)
                .whenScenarioStateIs(scenarioState)
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader(HttpHeaders.CONTENT_TYPE, contentType)
                        .withBody(body)));
    }

}