package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.domain.VersionInfo;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryJavaArchiveRepository;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.service.codesigner.JarVerifier;
import no.difi.move.kosmos.util.KosmosUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GetCurrentVersionActionTest {

    @InjectMocks
    private GetCurrentVersionAction target;

    @Mock
    private KosmosDirectoryJavaArchiveRepository repoMock;
    @Mock
    private File fileMock;
    @Mock
    private ActuatorService actuatorServiceMock;
    @Mock
    private JarVerifier jarVerifierMock;

    @Test
    public void apply_ApplicationIsNull_ShouldThrow() {
        given(actuatorServiceMock.getVersionInfo()).willReturn(
                VersionInfo.builder()
                        .resolved(true)
                        .version("1.0")
                        .build()
        );
        given(repoMock.getFile(anyString(), anyString())).willReturn(fileMock);
        assertThrows(NullPointerException.class, () -> target.apply(null));
    }

    @Test
    public void apply_VersionFound_ShouldUpdateCurrent() {
        given(actuatorServiceMock.getVersionInfo()).willReturn(
                VersionInfo.builder()
                        .resolved(true)
                        .version("1.0")
                        .build()
        );
        given(repoMock.getFile("1.0", KosmosUtils.DOWNLOAD_JAR_FILE_NAME)).willReturn(fileMock);
        Application application = new Application();

        assertThat(target.apply(application)).isSameAs(application);

        assertThat(application.getCurrent())
                .isEqualTo(new ApplicationMetadata().setVersion("1.0").setFile(fileMock));
        verify(repoMock, never()).getAllowlistVersion();
    }

    @Test
    public void apply_NoVersionFound_ShouldNotUpdateCurrent() {
        given(actuatorServiceMock.getVersionInfo())
                .willReturn(VersionInfo.builder()
                        .resolved(false).build());
        Application application = new Application();

        assertThat(target.apply(application)).isSameAs(application);

        assertThat(application.getCurrent()).isNull();
        verify(repoMock, never()).getFile(anyString(), anyString());
    }

    @Test
    public void apply_NotRunningAndAllowlistedJarIsVerified_ShouldUseAllowlistedVersion() {
        given(actuatorServiceMock.getVersionInfo())
                .willReturn(VersionInfo.builder()
                        .resolved(false).build());
        given(repoMock.getAllowlistVersion()).willReturn("2.0.0");
        given(repoMock.getFile("2.0.0", KosmosUtils.DOWNLOAD_JAR_FILE_NAME)).willReturn(fileMock);
        given(jarVerifierMock.isVerified("2.0.0", fileMock)).willReturn(true);
        Application application = new Application();

        assertThat(target.apply(application)).isSameAs(application);

        assertThat(application.getCurrent())
                .isEqualTo(new ApplicationMetadata().setVersion("2.0.0").setFile(fileMock));
    }

    @Test
    public void apply_NotRunningAndAllowlistedJarIsNotVerified_ShouldNotUseAllowlistedVersion() {
        given(actuatorServiceMock.getVersionInfo())
                .willReturn(VersionInfo.builder()
                        .resolved(false).build());
        given(repoMock.getAllowlistVersion()).willReturn("2.0.0");
        given(repoMock.getFile("2.0.0", KosmosUtils.DOWNLOAD_JAR_FILE_NAME)).willReturn(fileMock);
        given(jarVerifierMock.isVerified("2.0.0", fileMock)).willReturn(false);
        Application application = new Application();

        assertThat(target.apply(application)).isSameAs(application);

        assertThat(application.getCurrent()).isNull();
    }

    @Test
    public void apply_RunningVersion_ShouldNotVerifyJar() {
        given(actuatorServiceMock.getVersionInfo()).willReturn(
                VersionInfo.builder()
                        .resolved(true)
                        .version("1.0")
                        .build()
        );
        given(repoMock.getFile("1.0", KosmosUtils.DOWNLOAD_JAR_FILE_NAME)).willReturn(fileMock);

        target.apply(new Application());

        // the jar is verified when it is started, e.g. on rollback
        verify(jarVerifierMock, never()).isVerified(any(), any());
    }
}
