package no.difi.move.deploymanager.repo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeployDirectoryRepo {

    private final DeployManagerProperties properties;

    public File getFile(String version, String name) {
        File root = getOrCreateHomeFolder();
        return new File(root, String.format(name, version));
    }

    private File getOrCreateHomeFolder() {
        File home = new File(properties.getIntegrasjonspunkt().getHome());

        if (home.mkdir()) {
            log.info("Created home folder: {}", home.getAbsolutePath());
        }
        return home;
    }

    @SneakyThrows
    public void blackList(File file) {
        try {
            if (doBlacklist(file).createNewFile()) {
                log.info("Blacklisted {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.debug("Could not blacklist {}", file.getAbsolutePath(), e);
        }
    }

    public boolean isBlackListed(File file) {
        final File blacklistFile = getBlacklistPath(file);
        if (blacklistFile.exists()) {
            try {
                LocalDateTime expires = LocalDateTime.parse(FileUtils.readFileToString(blacklistFile, StandardCharsets.UTF_8));
                log.debug("Blacklist expires at {}", expires);
                final boolean expired = expires.isBefore(LocalDateTime.now());
                if (expired) {
                    removeBlacklist(blacklistFile);
                }
                return !expired;
            } catch (IOException e) {
                log.warn("Could not get blacklist information", e);
            }
        }
        return false;
    }

    public File getBlacklistPath(File file) {
        return new File(file.getAbsolutePath().replaceFirst("jar$", "blacklisted"));
    }

    private void removeBlacklist(File blacklistFile) {
        boolean deleted = FileUtils.deleteQuietly(blacklistFile);
        if (deleted) {
            log.debug("Removed expired blacklist file {}", blacklistFile);
        } else {
            log.debug("Could not remove expired blacklist file {}", blacklistFile);
        }
    }

    private File doBlacklist(File file) {
        final File blacklistFile = getBlacklistPath(file);
        log.debug("Blacklist file pathname is {}", blacklistFile.getAbsolutePath());
        try (BufferedWriter writer = Files.newBufferedWriter(blacklistFile.toPath(), StandardCharsets.UTF_8)) {
            int durationInHours = properties.getBlacklist().getDurationInHours();
            log.debug("Blacklist duration is {} hours", durationInHours);
            LocalDateTime expires = LocalDateTime.now().plusHours(durationInHours);
            log.debug("Blacklisting {} until {}", file.getName(), expires);
            writer.write(expires.toString());
        } catch (IOException e) {
            log.warn("Could not blacklist {}", file.getName());
        }
        return blacklistFile;
    }

    @SneakyThrows
    public void whitelist(File file, String fileName) {
        try {
            if (doWhitelist(file, fileName).createNewFile()) {
                log.info("Whitelisted {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.debug("Could not whitelist {}", file.getAbsolutePath(), e);
        }
    }

    private File doWhitelist(File file, String fileName)  {
        File whitelistFile = new File(properties.getIntegrasjonspunkt().getHome() + "/" + fileName);
        log.debug("Whitelist file pathname is {}", whitelistFile.getAbsolutePath());
        try(BufferedWriter writer = Files.newBufferedWriter(whitelistFile.toPath(), StandardCharsets.UTF_8)) {
            LocalDateTime created = LocalDateTime.now();
            log.debug("Whitelist {} created: {}", file.getName(), created);
            writer.write(created.toString());
        } catch (IOException e) {
            log.warn("Could not whitelist {}", file.getName());
        }
        return whitelistFile;
    }

    public File getWhitelistFile() {
        String[] filesNames;
        File f = new File(properties.getIntegrasjonspunkt().getHome());
        FilenameFilter filter = (f1, name) -> name.endsWith(".whitelisted");
        filesNames = f.list(filter);

        if(filesNames.length < 1) {
            return null;
        } else
            return new File(filesNames[0]);
    }

    public String getWhitelistVersion() {
        File file = getWhitelistFile();
        if(file != null){
            return file
                    .getName()
                    .replaceFirst(".whitelisted", "")
                    .replace("integrasjonspunkt-", "");
        } else {
            return null;
        }
    }

    public void removeWhitelist(File whitelistPath) {
        boolean deleted = FileUtils.deleteQuietly(whitelistPath);
        if (deleted) {
            log.debug("Removed whitelist file {}", whitelistPath);
        } else {
            log.debug("Could not remove whitelist file {}", whitelistPath);
        }
    }
}
