package no.difi.move.kosmos.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthStatusTest {
    @Test
    public void testFromString() {
        assertThat(HealthStatus.fromString("UP")).isSameAs(HealthStatus.UP);
        assertThat(HealthStatus.fromString("DOWN")).isSameAs(HealthStatus.DOWN);
        assertThat(HealthStatus.fromString("UNKNOWN")).isSameAs(HealthStatus.UNKNOWN);
        assertThat(HealthStatus.fromString("xxx")).isSameAs(HealthStatus.UNKNOWN);
        assertThat(HealthStatus.fromString(null)).isSameAs(HealthStatus.UNKNOWN);
    }
}
