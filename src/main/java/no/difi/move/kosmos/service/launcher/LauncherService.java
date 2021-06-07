package no.difi.move.kosmos.service.launcher;

import no.difi.move.kosmos.service.launcher.dto.LaunchResult;

public interface LauncherService {

    LaunchResult launchIntegrasjonspunkt(String jarPath);
}
