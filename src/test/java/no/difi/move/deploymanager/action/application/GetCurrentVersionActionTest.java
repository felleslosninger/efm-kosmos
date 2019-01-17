package no.difi.move.deploymanager.action.application;

import lombok.SneakyThrows;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class GetCurrentVersionActionTest {

    @InjectMocks private GetCurrentVersionAction target;

    @Mock private DeployManagerProperties propertiesMock;
    @Mock private DeployDirectoryRepo repoMock;
    @Mock private Application applicationMock;
    @Mock private File fileMock;

    @Captor private ArgumentCaptor<ApplicationMetadata> applicationMetadataArgumentCaptor;

    private Properties metadata;

    @Before
    @SneakyThrows(IOException.class)
    public void before() {
        given(propertiesMock.getRepository()).willReturn("staging");

        metadata = new Properties();
        metadata.setProperty("version", "1.0");
        metadata.setProperty("sha1", "sha1value");
        metadata.setProperty("filename", "file.jar");

        given(repoMock.getFile(any())).willReturn(fileMock);
        given(repoMock.getMetadata()).willReturn(metadata);
    }

    @Test(expected = NullPointerException.class)
    public void apply_calledOnNull_shouldThrow() {
        target.apply(null);
    }

    @Test
    public void apply_getMetaDataThrowsIOException_shouldThrow() throws IOException {
        IOException exception = new IOException("test exception");
        given(repoMock.getMetadata()).willThrow(exception);

        assertThatThrownBy(() -> target.apply(applicationMock))
                .isInstanceOf(DeployActionException.class)
                .hasMessage("Failed to get current version")
                .hasCause(exception);
    }

    @Test
    public void apply_shouldUpdateCurrent() {
        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(applicationMock).setCurrent(applicationMetadataArgumentCaptor.capture());
        verify(repoMock).getFile("file.jar");

        ApplicationMetadata captorValue = applicationMetadataArgumentCaptor.getValue();

        assertThat(captorValue.getVersion()).isEqualTo("1.0");
        assertThat(captorValue.getRepositoryId()).isEqualTo("staging");
        assertThat(captorValue.getSha1()).isEqualTo("sha1value");
        assertThat(captorValue.getFile()).isSameAs(fileMock);
    }

    @Test
    public void apply_whenNoFilename() {
        metadata.remove("filename");

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(applicationMock).setCurrent(applicationMetadataArgumentCaptor.capture());
        verify(repoMock, never()).getFile(any());

        ApplicationMetadata captorValue = applicationMetadataArgumentCaptor.getValue();
        assertThat(captorValue.getFile()).isNull();
    }

    @Test
    public void apply_whenNoVersion() {
        metadata.remove("version");

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(applicationMock).setCurrent(applicationMetadataArgumentCaptor.capture());

        ApplicationMetadata captorValue = applicationMetadataArgumentCaptor.getValue();
        assertThat(captorValue.getVersion()).isEqualTo("none");
    }
}
