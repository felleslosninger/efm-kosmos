package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.difi.move.deploymanager.service.launcher.LauncherServiceImpl;
import no.difi.move.deploymanager.service.launcher.dto.LaunchResult;
import no.difi.move.deploymanager.service.launcher.dto.LaunchStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.*;

@RequiredArgsConstructor
public class LauncherSteps {

    private final LauncherServiceImpl launcherServiceSpy;
    private final ResultCaptor<LaunchResult> launchResultResultCaptor = new ResultCaptor<>(LaunchResult.class);

    @Before
    @SneakyThrows
    public void before() {
        doAnswer(launchResultResultCaptor).when(launcherServiceSpy).launchIntegrasjonspunkt(any());
    }

    @After
    public void after() {
        reset(launcherServiceSpy);
        launchResultResultCaptor.reset();
    }

    @Then("^no JAR is launched$")
    public void noJARIsLaunched() {
        verify(launcherServiceSpy, never()).launchIntegrasjonspunkt(any());
    }

    @Then("^the \"([^\"]*)\" is successfully launched$")
    public void theJARIsSuccessfullyLaunched(String jarName) {
        verify(launcherServiceSpy).launchIntegrasjonspunkt(endsWith(jarName));

        LaunchResult launchResult = launchResultResultCaptor.getLastValue();
        assertThat(launchResult.getStatus()).isSameAs(LaunchStatus.SUCCESS);
    }
}
