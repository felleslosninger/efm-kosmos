package no.difi.move.deploymanager.repo;

import java.nio.file.Path;
import java.util.List;

public interface NexusRepo {
    void downloadJAR(String version, Path destination);
    byte[] getChecksum(String version, String classifier);
    List<String> downloadPublicKeys();
    String downloadSignature(String version);
}
