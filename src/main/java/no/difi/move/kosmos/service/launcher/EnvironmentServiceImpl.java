package no.difi.move.kosmos.service.launcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.config.KosmosProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentServiceImpl implements EnvironmentService {

    private final KosmosProperties properties;

    @Override
    public Map<String, String> getChildProcessEnvironment() {
        log.info("Preparing application environment");
        Map<String, String> environment = new HashMap<>(System.getenv());
        return calculateFilteredChildProcessEnvironment(environment);
    }

    Map<String, String> calculateFilteredChildProcessEnvironment(Map<String, String> environment) {
        List<String> exclusions = environment.keySet().stream()
                .filter(k -> properties.getEnvironment().getPrefixesRemovedFromChildProcess().stream()
                        .anyMatch(k::startsWith))
                .collect(Collectors.toList());
        log.debug("Excluding the following variables from child process environment: {}", exclusions);
        exclusions.forEach(e -> environment.put(e, null));
        return environment;
    }

}
