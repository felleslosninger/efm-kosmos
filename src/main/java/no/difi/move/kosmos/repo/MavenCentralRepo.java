package no.difi.move.kosmos.repo;

import java.nio.file.Path;

public interface MavenCentralRepo {

    void downloadJAR(String version, Path destination);

    byte[] getChecksum(String version, String classifier);

    String downloadSignature(String version);
}
