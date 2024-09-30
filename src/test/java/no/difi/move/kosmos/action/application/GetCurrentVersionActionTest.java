package no.difi.move.kosmos.action.application;

import no.difi.move.kosmos.domain.VersionInfo;
import no.difi.move.kosmos.domain.application.Application;
import no.difi.move.kosmos.domain.application.ApplicationMetadata;
import no.difi.move.kosmos.repo.KosmosDirectoryRepo;
import no.difi.move.kosmos.service.actuator.ActuatorService;
import no.difi.move.kosmos.util.KosmosUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
    private KosmosDirectoryRepo repoMock;
    @Mock
    private Application applicationMock;
    @Mock
    private File fileMock;
    @Mock
    private ActuatorService actuatorServiceMock;

    @Captor
    private ArgumentCaptor<ApplicationMetadata> applicationMetadataArgumentCaptor;

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
        given(repoMock.getFile(anyString(), anyString())).willReturn(fileMock);

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(applicationMock).setCurrent(applicationMetadataArgumentCaptor.capture());
        verify(repoMock).getFile("1.0", KosmosUtils.DOWNLOAD_JAR_FILE_NAME);
    }

    @Test
    public void apply_NoVersionFound_ShouldNotUpdateCurrent() {
        given(actuatorServiceMock.getVersionInfo())
                .willReturn(VersionInfo.builder()
                        .resolved(false).build());

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);
        verify(applicationMock, never()).setCurrent(any());
        verify(repoMock, never()).getFile(anyString(), anyString());
    }
}
