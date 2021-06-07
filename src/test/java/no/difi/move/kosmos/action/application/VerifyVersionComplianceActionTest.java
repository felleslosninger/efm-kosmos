package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.runners.Parameterized.Parameters;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(Enclosed.class)
public class VerifyVersionComplianceActionTest {

    final static IntegrasjonspunktProperties IP_PROPERTIES_MOCK = mock(IntegrasjonspunktProperties.class);
    final static ApplicationMetadata METADATA_MOCK = mock(ApplicationMetadata.class);
    final static Application APPLICATION_SPY = spy(new Application());
    final static KosmosProperties PROPERTIES = mock(KosmosProperties.class);
    final static KosmosDirectoryRepo REPO = mock(KosmosDirectoryRepo.class);

    final static VerifyVersionComplianceAction TARGET = new VerifyVersionComplianceAction(PROPERTIES, REPO);

    @RunWith(Parameterized.class)
    public static class NonCompliantTests {

        private final String supportedMajorVersion;
        private final String latestVersion;

        public NonCompliantTests(String supportedMajorVersion, String latestVersion) {
            this.supportedMajorVersion = supportedMajorVersion;
            this.latestVersion = latestVersion;
        }

        @Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {
                            "2", null
                    },
                    {
                            "1", "2.0.0"
                    },
                    {
                            "1", "2.0.0-SNAPSHOT"
                    },
                    {
                            "1", "beta"
                    }
            });
        }

        @Test
        public void apply_VersionDoesNotComplyWithSupportedMajor_ShouldThrow() {
            given(PROPERTIES.getIntegrasjonspunkt()).willReturn(IP_PROPERTIES_MOCK);
            given(APPLICATION_SPY.getLatest()).willReturn(METADATA_MOCK);
            given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn(supportedMajorVersion);
            given(METADATA_MOCK.getVersion()).willReturn(latestVersion);
            assertThatThrownBy(() -> TARGET.apply(APPLICATION_SPY))
                    .isInstanceOf(KosmosActionException.class);
        }

    }

    public static class CompliantTests {

        @Before
        public void setUp() {
            given(PROPERTIES.getIntegrasjonspunkt()).willReturn(IP_PROPERTIES_MOCK);
            given(APPLICATION_SPY.getLatest()).willReturn(METADATA_MOCK);
        }

        @Test
        public void apply_NoSupportedMajorVersionSet_ShouldPassVerification() {
            given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn(null);
            given(METADATA_MOCK.getVersion()).willReturn("2");
            assertThat(TARGET.apply(APPLICATION_SPY)).isSameAs(APPLICATION_SPY);
        }

        @Test
        public void apply_SupportedMajorVersion_ShouldPassVerification() {
            given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn("2");
            given(METADATA_MOCK.getVersion()).willReturn("2.3.4-SNAPSHOT");
            assertThat(TARGET.apply(APPLICATION_SPY)).isSameAs(APPLICATION_SPY);
        }
    }
}