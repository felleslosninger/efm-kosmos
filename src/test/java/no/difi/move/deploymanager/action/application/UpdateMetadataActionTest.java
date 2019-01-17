package no.difi.move.deploymanager.action.application;

import lombok.SneakyThrows;
import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.DeployDirectoryRepo;
import no.difi.move.deploymanager.service.laucher.dto.LaunchResult;
import no.difi.move.deploymanager.service.laucher.dto.LaunchStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateMetadataActionTest {

    @InjectMocks private UpdateMetadataAction target;

    @Mock private DeployDirectoryRepo repoMock;
    @Mock private Properties metaDataMock;
    @Mock private File fileMock;

    private Application application;

    @Before
    public void setUp() throws Exception {
        application = new Application()
                .setLatest(new ApplicationMetadata()
                        .setVersion("1.0")
                        .setSha1("sha1")
                        .setRepositoryId("staging")
                        .setFile(fileMock)
                ).setLaunchResult(new LaunchResult()
                        .setStatus(LaunchStatus.SUCCESS)
                );

        given(fileMock.getName()).willReturn("filename.jar");
        given(repoMock.getMetadata()).willReturn(metaDataMock);
    }

    @Test(expected = NullPointerException.class)
    public void apply_toNull_shouldThrow() {
        target.apply(null);
    }

    @Test(expected = NullPointerException.class)
    public void apply_whenLatestIsNull_shouldThrow() {
        application.setLatest(null);
        target.apply(null);
    }

    @Test
    @SneakyThrows(IOException.class)
    public void apply_whenNullLauchResult_shouldReturn() {
        application.setLaunchResult(null);
        assertThat(target.apply(application)).isSameAs(application);
        verify(repoMock, never()).setMetadata(any());
    }

    @Test
    @SneakyThrows(IOException.class)
    public void apply_whenLauchStatusIsFailure_shouldReturn() {
        application.getLaunchResult().setStatus(LaunchStatus.FAILED);
        assertThat(target.apply(application)).isSameAs(application);
        verify(repoMock, never()).setMetadata(any());
    }

    @Test
    @SneakyThrows
    public void apply_whenSetProperiesFailes_shouldThrow() {
        IOException exception = new IOException("dummy");
        doThrow(exception).when(repoMock).setMetadata(any());

        assertThatThrownBy(() -> target.apply(application))
                .isInstanceOf(DeployActionException.class)
                .hasMessage("Could not update metadata.")
                .hasCause(exception);
    }

    @Test
    public void apply_successful_shouldSetMetadata() throws IOException {
        assertThat(target.apply(application)).isSameAs(application);

        verify(metaDataMock).setProperty("version", "1.0");
        verify(metaDataMock).setProperty("sha1", "sha1");
        verify(metaDataMock).setProperty("repositoryId", "staging");
        verify(metaDataMock).setProperty("filename", "filename.jar");
        verify(repoMock).setMetadata(metaDataMock);
    }
}
