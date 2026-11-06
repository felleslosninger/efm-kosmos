package no.difi.move.kosmos.util;

import java.util.regex.Pattern;

public class KosmosUtils {
    public static final String DOWNLOAD_JAR_FILE_NAME = "integrasjonspunkt-%s.jar";
    // unvalidated downloads are kept here, and only moved to DOWNLOAD_JAR_FILE_NAME once validated
    public static final String UNVALIDATED_JAR_FILE_NAME = "integrasjonspunkt-%s.jar.download";

    // versions are used in file names and download URLs, so only allow plain semver (e.g. 2.31.0, 2.31.0-SNAPSHOT)
    // release tags have a leading "v" (e.g. v4.2.0), and the version file uses the tag as version
    private static final Pattern VALID_VERSION = Pattern.compile("^[vV]?\\d+\\.\\d+\\.\\d+(-[A-Za-z0-9.]+)?$");

    private KosmosUtils() {
    }

    public static boolean isValidVersion(String version) {
        return version != null && VALID_VERSION.matcher(version).matches();
    }

    public static String requireValidVersion(String version) {
        if (!isValidVersion(version)) {
            throw new IllegalArgumentException("Invalid version '%s', expected a semantic version like 2.31.0 or v4.2.0".formatted(version));
        }
        return version;
    }
}
