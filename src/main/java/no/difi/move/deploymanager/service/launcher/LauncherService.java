package no.difi.move.deploymanager.service.launcher;

import no.difi.move.deploymanager.service.launcher.dto.LaunchResult;

public interface LauncherService {

    LaunchResult launchIntegrasjonspunkt(String jarPath);
}
