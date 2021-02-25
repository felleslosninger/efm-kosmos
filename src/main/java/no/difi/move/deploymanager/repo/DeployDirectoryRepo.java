package no.difi.move.deploymanager.repo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeployDirectoryRepo {

    private static final String META_PROPERTIES = "meta.properties";

    private final DeployManagerProperties properties;

    public Properties getMetadata() throws IOException {
        File propertiesFile = getOrCreateFile(META_PROPERTIES);

        Properties props = new Properties();

        try (InputStream is = new FileInputStream(propertiesFile)) {
            props.load(is);
        }

        return props;
    }

    public void setMetadata(Properties properties) throws IOException {
        File propertiesFile = getOrCreateFile(META_PROPERTIES);

        try (FileOutputStream os = new FileOutputStream(propertiesFile)) {
            properties.store(os, "Automatically generated");
        }
    }

    public File getFile(String filename) {
        File homeFolder = getOrCreateHomeFolder();
        log.info("Home folder is set to: {}", homeFolder.getAbsolutePath());
        File file = new File(homeFolder, filename);
        if (file.exists()) {
            return file;
        }

        throw new DeployActionException(String.format("File not found: %s", file.getAbsolutePath()));
    }

    private File getOrCreateFile(String file) throws IOException {
        File homeFolder = getOrCreateHomeFolder();
        File propertiesFile = new File(homeFolder, file);
        if (propertiesFile.createNewFile()) {
            log.info("Created file: {}", propertiesFile.getAbsolutePath());
        }
        return propertiesFile;
    }

    private File getOrCreateHomeFolder() {
        File home = new File(properties.getHome());

        if (home.mkdir()) {
            log.info("Created home folder: {}", home.getAbsolutePath());
        }
        return home;
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
