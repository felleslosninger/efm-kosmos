package no.difi.move.deploymanager.cucumber;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import lombok.SneakyThrows;
import no.difi.move.deploymanager.service.laucher.LauncherServiceImpl;
import no.difi.move.deploymanager.service.laucher.dto.LaunchResult;
import no.difi.move.deploymanager.service.laucher.dto.LaunchStatus;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class LauncherSteps {

    @Autowired
    private LauncherServiceImpl launcherServiceSpy;

    private final ResultCaptor<LaunchResult> launchResultResultCaptor = new ResultCaptor<>(LaunchResult.class);

    @Before
    @SneakyThrows
    public void before() {
        reset(launcherServiceSpy);
        launchResultResultCaptor.reset();
        doAnswer(launchResultResultCaptor).when(launcherServiceSpy).launchIntegrasjonspunkt(any());
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
