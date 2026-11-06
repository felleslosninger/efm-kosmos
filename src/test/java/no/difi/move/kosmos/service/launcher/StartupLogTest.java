package no.difi.move.kosmos.service.launcher;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

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
    public void consume() {
        StartupLog startupLog = new StartupLog(false);
        startupLog.consume(new ByteArrayInputStream("bla bla\r\nHallo\nHei".getBytes(Charset.defaultCharset())));
        assertThat(startupLog.getLog()).isEqualTo("bla bla\nHallo\nHei\n");
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