package no.difi.move.deploymanager.service.launcher;

import lombok.RequiredArgsConstructor;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnvironmentServiceImpl implements EnvironmentService {

    private final DeployManagerProperties properties;

    @Override
    public Map<String, String> getChildProcessEnvironment() {
        Map<String, String> environment = new HashMap<>(System.getenv());
        List<String> exclusions = environment.keySet().stream()
                .filter(k -> properties.getEnvironment().getPrefixesRemovedFromChildProcess().stream()
                        .anyMatch(k::startsWith))
                .collect(Collectors.toList());
        exclusions.forEach(e -> environment.put(e, null));
        return environment;
    }
}
