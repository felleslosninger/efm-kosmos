package no.difi.move.deploymanager.service.laucher;

import no.difi.move.deploymanager.service.laucher.dto.LaunchResult;

public interface LauncherService {

    LaunchResult launchIntegrasjonspunkt(String jarPath);
}
