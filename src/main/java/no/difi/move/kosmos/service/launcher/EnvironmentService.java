package no.difi.move.kosmos.service.launcher;

import java.util.Map;

public interface EnvironmentService {

    Map<String, String> getChildProcessEnvironment();

}
