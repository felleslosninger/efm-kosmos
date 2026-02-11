package no.difi.move.kosmos.repo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KosmosDirectoryRepoTest {

    KosmosDirectoryRepo repo = new KosmosDirectoryRepo(null);

    @Test
    void getSemanticVersion() {
        assertNull(repo.getSemanticVersion("filename-1"));
        assertNull(repo.getSemanticVersion("filename-1.2"));
        assertEquals("1.2.3", repo.getSemanticVersion("filename-1.2.3"));
        assertEquals("2.28.5", repo.getSemanticVersion("filename-2.28.5"));
        assertEquals("3.0.0", repo.getSemanticVersion("filename-3.0.0"));
        assertEquals("4.0.0", repo.getSemanticVersion("filename-4.0.0"));
        assertEquals("4.0.0-beta", repo.getSemanticVersion("filename-4.0.0-beta"));
        // from IPv4 we use tagging and they have not been consistent, so we need to ignore leading "v" og "V"
        assertEquals("4.0.1", repo.getSemanticVersion("filename-v4.0.1"));
        assertEquals("4.1.0", repo.getSemanticVersion("filename-V4.1.0"));
        assertEquals("4.2.1-beta", repo.getSemanticVersion("filename-v4.2.1-beta"));
        assertEquals("4.3.0-rc", repo.getSemanticVersion("filename-V4.3.0-rc"));
    }

}
