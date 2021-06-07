package no.difi.move.kosmos.service.launcher;

import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.stream.LogOutputStream;

@Slf4j
class StartupLog extends LogOutputStream {

    private static final String VERBOSITY_PREFIX = "[INCLUDED]";
    private final boolean verbose;
    private final StringBuilder logBuilder = new StringBuilder();

    private boolean record = true;

    StartupLog(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    protected void processLine(String line) {
        if (record) {
            if (verbose) {
                log.info(VERBOSITY_PREFIX + " " + line);
            }

            logBuilder.append(line).append("\n");
        }
    }

    public String getLog() {
        return logBuilder.toString();
    }

    void stopRecording() {
        record = false;
    }
}
