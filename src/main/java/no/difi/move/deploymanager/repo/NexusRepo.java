package no.difi.move.deploymanager.repo;

import java.nio.file.Path;

public interface NexusRepo {
    void downloadJAR(String version, Path destination);
    byte[] getChecksum(String version, String classifier);
    String downloadPublicKey();
    String downloadSignature(String version);
}
