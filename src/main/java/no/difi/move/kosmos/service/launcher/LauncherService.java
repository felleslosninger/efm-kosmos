package no.difi.move.kosmos.service.launcher;

import no.difi.move.kosmos.service.launcher.dto.LaunchResult;

import java.io.File;

public interface LauncherService {

    /**
     * Starts the JAR file, but only if it can be verified against the signature for the version.
     */
    LaunchResult launchIntegrasjonspunkt(String version, File jarFile);
}
