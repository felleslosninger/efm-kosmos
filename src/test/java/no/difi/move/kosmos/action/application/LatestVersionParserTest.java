package no.difi.move.kosmos.action.application;


import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class LatestVersionParserTest {

    @Test
    public void parseYamlConfig() throws Exception {

        var itest = LatestVersionAction.fetchLatestVersions("itest");
        assertEquals("2.0.0", itest.latest());
        assertEquals("2.1.0-SNAPSHOT", itest.earlybird());

        var production = LatestVersionAction.fetchLatestVersions("production");
        assertEquals("4.0.0", production.latest());
        assertEquals("4.1.0-SNAPSHOT", production.earlybird());

    }

}
