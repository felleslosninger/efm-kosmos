package no.difi.move.deploymanager.repo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeployDirectoryRepo {

    private final DeployManagerProperties properties;

    public File getFile(String version) {
        File root = getOrCreateRoot();
        return new File(root, String.format("integrasjonspunkt-%s.jar", version));
    }

    private File getOrCreateRoot() {
        File root = new File(properties.getRoot());
        if (root.mkdir()) {
            log.info("Created root folder: {}", root.getAbsolutePath());
        }
        return root;
    }

    @SneakyThrows
    public void blackList(File file) {
        try {
            if (getBlackListedFile(file).createNewFile()) {
                log.info("Blacklisted {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.debug("Could not blacklist {}", file.getAbsolutePath(), e);
        }
    }

    public boolean isBlackListed(File file) {
        return getBlackListedFile(file).exists();
    }

    public File getBlackListedFile(File file) {
        return new File(file.getAbsolutePath().replaceFirst("jar$", "blacklisted"));
    }
}
