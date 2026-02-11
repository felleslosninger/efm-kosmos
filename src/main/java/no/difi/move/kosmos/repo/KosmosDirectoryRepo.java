package no.difi.move.kosmos.repo;

import com.vdurmont.semver4j.Semver;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.config.KosmosProperties;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class KosmosDirectoryRepo {

    private final KosmosProperties properties;
    private static String ALLOWLISTEDFILENAME = "integrasjonspunkt-%s.allowlisted";
    private static Pattern PATTERN = Pattern.compile("-[vV]?(\\d+.\\d+.\\d+[^.]*)");

    public File getFile(String version, String name) {
        File root = getOrCreateHomeFolder();
        return new File(root, name.formatted(version));
    }

    private File getOrCreateHomeFolder() {
        File home = new File(properties.getIntegrasjonspunkt().getHome());

        if (home.mkdir()) {
            log.info("Created home folder: {}", home.getAbsolutePath());
        }
        return home;
    }

    @SneakyThrows
    public void blockList(File file) {
        try {
            if (doBlocklist(file).createNewFile()) {
                log.info("Blocklisted {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.debug("Could not blocklist {}", file.getAbsolutePath(), e);
        }
    }

    public boolean isBlockListed(File file) {
        final File blocklistFile = getBlocklistPath(file);
        if (blocklistFile.exists()) {
            try {
                LocalDateTime expires = LocalDateTime.parse(FileUtils.readFileToString(blocklistFile, StandardCharsets.UTF_8));
                log.debug("Blocklist expires at {}", expires);
                final boolean expired = expires.isBefore(LocalDateTime.now());
                if (expired) {
                    removeBlocklist(blocklistFile);
                }
                return !expired;
            } catch (IOException e) {
                log.warn("Could not get blocklist information", e);
            }
        }
        return false;
    }

    public File getBlocklistPath(File file) {
        return new File(file.getAbsolutePath().replaceFirst("jar$", "blocklisted"));
    }

    private void removeBlocklist(File blocklistFile) {
        boolean deleted = FileUtils.deleteQuietly(blocklistFile);
        if (deleted) {
            log.debug("Removed expired blocklist file {}", blocklistFile);
        } else {
            log.debug("Could not remove expired blocklist file {}", blocklistFile);
        }
    }

    private File doBlocklist(File file) {
        final File blocklistFile = getBlocklistPath(file);
        log.debug("Blocklist file pathname is {}", blocklistFile.getAbsolutePath());
        try (BufferedWriter writer = Files.newBufferedWriter(blocklistFile.toPath(), StandardCharsets.UTF_8)) {
            int durationInHours = properties.getBlocklist().getDurationInHours();
            log.debug("Blocklist duration is {} hours", durationInHours);
            LocalDateTime expires = LocalDateTime.now().plusHours(durationInHours);
            log.debug("Blocklisting {} until {}", file.getName(), expires);
            writer.write(expires.toString());
        } catch (IOException e) {
            log.warn("Could not blocklist {}", file.getName());
        }
        return blocklistFile;
    }

    @SneakyThrows
    public void allowlist(File file, String version) {
        try {
            if (doAllowlist(file, version).createNewFile()) {
                log.info("Allowlisted {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Could not Allowlist {}", file.getAbsolutePath(), e);
        }
    }

    private File doAllowlist(File file, String version) {
        String fileName = ALLOWLISTEDFILENAME.formatted(version);
        File allowlistFile = new File(properties.getIntegrasjonspunkt().getHome() + "/" + fileName);
        log.debug("Allowlist file pathname is {}", allowlistFile.getAbsolutePath());
        try (BufferedWriter writer = Files.newBufferedWriter(allowlistFile.toPath(), StandardCharsets.UTF_8)) {
            LocalDateTime created = LocalDateTime.now();
            log.debug("Allowlist file: {} created: {}", file.getName(), created);
            writer.write(created.toString());
        } catch (IOException e) {
            log.warn("Could not Allowlist file: {}", file.getName());
        }
        return allowlistFile;
    }

    public File getAllowlistFile() {
        String[] filesNames;
        File f = new File(properties.getIntegrasjonspunkt().getHome());
        FilenameFilter filter = (f1, name) -> name.endsWith(".allowlisted");
        filesNames = f.list(filter);
        return Arrays.stream(filesNames)
                .map(s -> new Semver(getSemanticVersion(s)))
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .map(p -> new File(ALLOWLISTEDFILENAME.formatted(p.getValue())))
                .orElse(null);
    }

    public String getAllowlistVersion() {
        File file = getAllowlistFile();
        if (file != null) {
            return file
                    .getName()
                    .replaceFirst(".allowlisted", "")
                    .replace("integrasjonspunkt-", "");
        } else {
            return null;
        }
    }

    public void removeAllowlist(String version) {
        File allowlistPath = getFile(version, ALLOWLISTEDFILENAME);
        boolean deleted = FileUtils.deleteQuietly(allowlistPath);
        if (deleted) {
            log.debug("Removed Allowlist file {}", allowlistPath);
        } else {
            log.debug("Could not remove Allowlist file {}", allowlistPath);
        }
    }

    String getSemanticVersion(String filename) {
        Matcher matcher = PATTERN.matcher(filename);
        return matcher.find() ? matcher.group(1) : null;
    }

}
