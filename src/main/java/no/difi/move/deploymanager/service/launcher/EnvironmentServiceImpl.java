package no.difi.move.deploymanager.service.launcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentServiceImpl implements EnvironmentService {

    private final DeployManagerProperties properties;

    @Override
    public Map<String, String> getChildProcessEnvironment() {
        log.info("Preparing application environment");
        Map<String, String> environment = new HashMap<>(System.getenv());
        List<String> exclusions = environment.keySet().stream()
                .filter(k -> properties.getEnvironment().getPrefixesRemovedFromChildProcess().stream()
                        .anyMatch(k::startsWith))
                .collect(Collectors.toList());
        log.debug("Excludes the following variables: {}", exclusions);
        exclusions.forEach(e -> environment.put(e, null));
        return environment;
    }
}
