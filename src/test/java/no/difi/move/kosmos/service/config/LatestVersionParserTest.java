package no.difi.move.kosmos.service.config;


import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class LatestVersionParserTest {

    @Test
    public void parseYamlConfig() throws Exception {

        var yamlAsString = new String(LatestVersionParserTest.class
                .getResourceAsStream("/versions/latest-versions.yml")
                .readAllBytes());

        var environments = RefreshServiceImpl.parseLatestVersions(yamlAsString).integrasjonspunkt().environments();

        var itest = environments.get("itest");
        assertEquals("2.0.0", itest.latest());
        assertEquals("2.1.0-SNAPSHOT", itest.earlybird());

        var production = environments.get("production");
        assertEquals("4.0.0", production.latest());
        assertEquals("4.1.0-SNAPSHOT", production.earlybird());

    }

}
