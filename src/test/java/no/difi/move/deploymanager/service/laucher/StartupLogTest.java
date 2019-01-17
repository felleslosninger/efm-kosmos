package no.difi.move.deploymanager.service.laucher;

import no.difi.move.deploymanager.service.laucher.dto.LaunchStatus;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class StartupLogTest {

    @Test
    public void processLine() {
        StartupLog startupLog = new StartupLog(false);
        startupLog.processLine("bla bla");
        assertThat(startupLog.getStatus()).isSameAs(LaunchStatus.UNKNOWN);
    }

    @Test
    public void processLineWhenSuccess() {
        StartupLog startupLog = new StartupLog(false);
        startupLog.processLine("bla bla");
        assertThat(startupLog.getStatus()).isSameAs(LaunchStatus.UNKNOWN);
        startupLog.processLine("Started IntegrasjonspunktApplication");
        assertThat(startupLog.getStatus()).isSameAs(LaunchStatus.SUCCESS);
        startupLog.processLine("bla bla");
        assertThat(startupLog.getStatus()).isSameAs(LaunchStatus.SUCCESS);
    }

    @Test
    public void processLineWhenFailure() {
        StartupLog startupLog = new StartupLog(false);
        startupLog.processLine("bla bla");
        assertThat(startupLog.getStatus()).isSameAs(LaunchStatus.UNKNOWN);
        startupLog.processLine("Application startup failed");
        assertThat(startupLog.getStatus()).isSameAs(LaunchStatus.FAILED);
        startupLog.processLine("bla bla");
        assertThat(startupLog.getStatus()).isSameAs(LaunchStatus.FAILED);
    }

    @Test
    public void getLog() {
        StartupLog startupLog = new StartupLog(false);
        startupLog.processLine("bla bla");
        startupLog.processLine("Hallo");
        assertThat(startupLog.getLog()).isEqualTo("bla bla\nHallo\n");
    }

    @Test
    public void stopRecording() {
        StartupLog startupLog = new StartupLog(false);
        startupLog.processLine("bla bla");
        startupLog.stopRecording();
        startupLog.processLine("Hallo");
        assertThat(startupLog.getLog()).isEqualTo("bla bla\n");
    }
}