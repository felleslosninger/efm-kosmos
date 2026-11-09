package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.IntegrasjonspunktProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryJavaArchiveRepository;
import no.difi.move.kosmos.service.codesigner.JarVerifier;
import no.difi.move.kosmos.util.KosmosUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class VerifyVersionComplianceActionTest {

    final static IntegrasjonspunktProperties IP_PROPERTIES_MOCK = mock(IntegrasjonspunktProperties.class);
    final static ApplicationMetadata METADATA_MOCK = mock(ApplicationMetadata.class);
    final static Application APPLICATION_SPY = spy(new Application());
    final static KosmosProperties PROPERTIES = mock(KosmosProperties.class);
    final static KosmosDirectoryJavaArchiveRepository REPO = mock(KosmosDirectoryJavaArchiveRepository.class);

    final static JarVerifier JAR_VERIFIER = mock(JarVerifier.class);

    final static VerifyVersionComplianceAction TARGET = new VerifyVersionComplianceAction(PROPERTIES, REPO, JAR_VERIFIER);

    static Stream<Arguments> nonCompliantVersionsProvider() {
        return Stream.of(
                arguments("2", null),
                arguments("1", "2.0.0"),
                arguments("1", "2.0.0-SNAPSHOT"),
                arguments("1", "beta"));
    }

    @ParameterizedTest
    @MethodSource("nonCompliantVersionsProvider")
    public void apply_VersionDoesNotComplyWithSupportedMajor_ShouldThrow(String supportedMajorVersion, String latestVersion) {
        given(PROPERTIES.getIntegrasjonspunkt()).willReturn(IP_PROPERTIES_MOCK);
        given(APPLICATION_SPY.getLatest()).willReturn(METADATA_MOCK);
        given(IP_PROPERTIES_MOCK.getSupportedMajorVersion()).willReturn(supportedMajorVersion);
        given(METADATA_MOCK.getVersion()).willReturn(latestVersion);
        assertThatThrownBy(() -> TARGET.apply(APPLICATION_SPY))
                .isInstanceOf(KosmosActionException.class);
    }


    @Nested
    public class AllowlistFallbackTests {

        private final KosmosProperties properties = mock(KosmosProperties.class);
        private final KosmosDirectoryJavaArchiveRepository repo = mock(KosmosDirectoryJavaArchiveRepository.class);
        private final JarVerifier jarVerifier = mock(JarVerifier.class);
        private final File jarFile = new File("integrasjonspunkt-1.9.0.jar");
        private final VerifyVersionComplianceAction target = new VerifyVersionComplianceAction(properties, repo, jarVerifier);
        private Application application;

        @BeforeEach
        public void setUp() {
            IntegrasjonspunktProperties integrasjonspunktProperties = new IntegrasjonspunktProperties().setSupportedMajorVersion("1");
            given(properties.getIntegrasjonspunkt()).willReturn(integrasjonspunktProperties);
            given(repo.getAllowlistVersion()).willReturn("1.9.0");
            given(repo.getFile("1.9.0", KosmosUtils.DOWNLOAD_JAR_FILE_NAME)).willReturn(jarFile);
            application = new Application().setLatest(new ApplicationMetadata().setVersion("2.0.0"));
        }

        @Test
        public void apply_UnsupportedLatestAndAllowlistedJarIsVerified_ShouldUseAllowlistedVersion() {
            given(jarVerifier.isVerified("1.9.0", jarFile)).willReturn(true);

            assertThat(target.apply(application).getLatest())
                    .isEqualTo(new ApplicationMetadata().setVersion("1.9.0").setFile(jarFile));
        }

        @Test
        public void apply_UnsupportedLatestAndAllowlistedJarIsNotVerified_ShouldThrow() {
            given(jarVerifier.isVerified("1.9.0", jarFile)).willReturn(false);

            assertThatThrownBy(() -> target.apply(application))
                    .isInstanceOf(KosmosActionException.class)
                    .hasMessageStartingWith("Latest version (2) is not supported yet");
            assertThat(application.getLatest().getVersion()).isEqualTo("2.0.0");
        }
    }

    @Nested
    public class CompliantTests {

        @BeforeEach
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
