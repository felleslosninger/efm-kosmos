package no.difi.move.kosmos.service.launcher;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

@Slf4j
class StartupLog {

    private static final String VERBOSITY_PREFIX = "[INCLUDED]";
    private final boolean verbose;
    private final StringBuilder logBuilder = new StringBuilder();

    private volatile boolean record = true;

    StartupLog(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Reads the stream line by line until it ends. The stream must be read to the end
     * even after recording has stopped, otherwise the child process blocks once the pipe buffer is full.
     */
    void consume(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            log.debug("Stopped reading application output", e);
        }
    }

    void processLine(String line) {
        if (record) {
            if (verbose) {
                log.info(VERBOSITY_PREFIX + " " + line);
            }

            synchronized (logBuilder) {
                logBuilder.append(line).append("\n");
            }
        }
    }

    public String getLog() {
        synchronized (logBuilder) {
            return logBuilder.toString();
        }
    }

    void stopRecording() {
        record = false;
    }
}
