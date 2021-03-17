package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.domain.VersionInfo;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.service.actuator.ActuatorService;
import no.difi.move.deploymanager.util.DeployUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GetCurrentVersionActionTest {

    @InjectMocks
    private GetCurrentVersionAction target;

    @Mock
    private DeployDirectoryRepo repoMock;
    @Mock
    private Application applicationMock;
    @Mock
    private File fileMock;
    @Mock
    private ActuatorService actuatorServiceMock;

    @Captor
    private ArgumentCaptor<ApplicationMetadata> applicationMetadataArgumentCaptor;

    @Before
    public void before() {
        given(actuatorServiceMock.getVersionInfo()).willReturn(
                VersionInfo.builder()
                        .resolved(true)
                        .version("1.0")
                        .build()
        );
        given(repoMock.getFile(anyString(), anyString())).willReturn(fileMock);
    }

    @Test(expected = NullPointerException.class)
    public void apply_ApplicationIsNull_ShouldThrow() {
        target.apply(null);
    }

    @Test
    public void apply_VersionFound_ShouldUpdateCurrent() {
        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);
        verify(applicationMock).setCurrent(applicationMetadataArgumentCaptor.capture());
        verify(repoMock).getFile("1.0", DeployUtils.DOWNLOAD_JAR_FILE_NAME);
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
