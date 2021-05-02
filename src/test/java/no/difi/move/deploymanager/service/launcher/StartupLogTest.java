package no.difi.move.deploymanager.service.launcher;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class StartupLogTest {

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