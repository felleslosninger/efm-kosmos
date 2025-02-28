package no.difi.move.kosmos.service.config;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.*;

@Disabled("This test is for manual debugging, it does a live download and parses the result")
@SpringBootTest(properties = {
        "kosmos.integrasjonspunkt.autoRefresh=true",
        "kosmos.integrasjonspunkt.versionsURL=https://raw.githubusercontent.com/felleslosninger/efm-integrasjonspunkt/refs/heads/feature-MOVE-3684-integrasjonspunkt-v3/latest-versions.yml" })
public class RefreshServiceImplManualIT {

    @Autowired
    private RefreshService target;

    @Test
    public void manual_liveDownloadAndParsingTest() {
        var config = target.refreshConfig();
        assertNotNull(config);
        assertNull(config.integrasjonspunkt().environments().get("unknown"));
        assertEquals("2.1.0-SNAPSHOT", config.integrasjonspunkt().environments().get("itest").earlybird());
        assertEquals("4.1.0-SNAPSHOT", config.integrasjonspunkt().environments().get("production").earlybird());
    }

}