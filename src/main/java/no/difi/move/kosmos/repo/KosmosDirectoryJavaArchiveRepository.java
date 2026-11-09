package no.difi.move.kosmos.repo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.difi.move.kosmos.config.KosmosProperties;
import org.apache.commons.io.FileUtils;
import org.semver4j.Semver;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class KosmosDirectoryJavaArchiveRepository {

    private final KosmosProperties properties;
    private final Clock clock;
    private static final String ALLOWLISTEDFILENAME = "integrasjonspunkt-%s.allowlisted";
    private static final Pattern PATTERN = Pattern.compile("-[vV]?(\\d+.\\d+.\\d+[^.]*)");

    public File getFile(String version, String name) {
        File root = getOrCreateHomeFolder();
        // the version may come from external sources, never let it place files outside the home folder
        Path rootPath = root.toPath().toAbsolutePath().normalize();
        Path filePath = rootPath.resolve(name.formatted(version)).normalize();
        if (!rootPath.equals(filePath.getParent())) {
            throw new IllegalArgumentException("Version '%s' gives a file outside the home folder %s".formatted(version, rootPath));
        }
        // return the checked path, the original could resolve elsewhere through a symlink followed by ".."
        return filePath.toFile();
    }

    private File getOrCreateHomeFolder() {
        Path home = Path.of(properties.getIntegrasjonspunkt().getHome());
        Path absolute = home.toAbsolutePath();
        if (!Files.isDirectory(absolute)) {
            if (Files.exists(absolute)) {
                throw new IllegalStateException("The home folder %s is not a folder".formatted(absolute));
            }
            try {
                // the parent folders may be missing too, File.mkdir() would then fail without telling
                Files.createDirectories(absolute);
                log.info("Created home folder: {}", absolute);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create the home folder %s: %s".formatted(absolute, e), e);
            }
        }
        if (!Files.isReadable(absolute)) {
            throw new IllegalStateException("The home folder %s cannot be read".formatted(absolute));
        }
        return home.toFile();
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
                final boolean expired = expires.isBefore(LocalDateTime.now(clock));
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
            LocalDateTime expires = LocalDateTime.now(clock).plusHours(durationInHours);
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
        File allowlistFile = getFile(version, ALLOWLISTEDFILENAME);
        log.debug("Allowlist file pathname is {}", allowlistFile.getAbsolutePath());
        try (BufferedWriter writer = Files.newBufferedWriter(allowlistFile.toPath(), StandardCharsets.UTF_8)) {
            LocalDateTime created = LocalDateTime.now(clock);
            log.debug("Allowlist file: {} created: {}", file.getName(), created);
            writer.write(created.toString());
        } catch (IOException e) {
            log.warn("Could not Allowlist file: {}", file.getName());
        }
        return allowlistFile;
    }

    public File getAllowlistFile() {
        File home = getOrCreateHomeFolder();
        FilenameFilter filter = (f1, name) -> name.endsWith(".allowlisted");
        String[] filesNames = home.list(filter);
        if (filesNames == null) {
            throw new IllegalStateException("Could not list the files in the home folder %s".formatted(home.getAbsolutePath()));
        }
        // anyone who can write to the home folder can add files, so skip names without a valid version instead of failing
        return Arrays.stream(filesNames)
                .map(this::getSemanticVersion)
                .filter(Objects::nonNull)
                .map(Semver::parse)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .findFirst()
                .map(p -> new File(ALLOWLISTEDFILENAME.formatted(p.getVersion())))
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
