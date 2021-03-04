package no.difi.move.deploymanager.service.launcher;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.service.launcher.dto.LaunchStatus;
import org.zeroturnaround.exec.stream.LogOutputStream;

@Slf4j
class StartupLog extends LogOutputStream {

    private final static String VERBOSITY_PREFIX = "[INCLUDED]";
    private final boolean verbose;
    private final StringBuilder logBuilder = new StringBuilder();

    @Getter
    private LaunchStatus status = LaunchStatus.UNKNOWN;
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

            if (line.contains("Application startup failed")) {
                status = LaunchStatus.FAILED;
            }

            if (line.contains("Started IntegrasjonspunktApplication")) {
                status = LaunchStatus.SUCCESS;
            }
        }
    }

    public String getLog() {
        return logBuilder.toString();
    }

    void stopRecording() {
        record = false;
    }
}
