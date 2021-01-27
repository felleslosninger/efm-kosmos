package no.difi.move.deploymanager.service.launcher;

import java.util.Map;

public interface EnvironmentService {

    Map<String, String> getChildProcessEnvironment();

}
