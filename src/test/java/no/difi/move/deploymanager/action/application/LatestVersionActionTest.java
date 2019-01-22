package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import no.difi.move.deploymanager.repo.dto.ApplicationMetadataResource;
import no.difi.move.deploymanager.repo.NexusRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LatestVersionActionTest {

    @Mock private DeployManagerProperties propertiesMock;
    @Mock private NexusRepo nexusRepoMock;
    @Mock private Application applicationMock;

    @Captor private ArgumentCaptor<ApplicationMetadata> applicationMetadataArgumentCaptor;

    @InjectMocks private LatestVersionAction target;

    @Before
    public void before() {
        given(propertiesMock.getRepository()).willReturn("staging");
        given(nexusRepoMock.getApplicationMetadata()).willReturn(new ApplicationMetadataResource()
                .setBaseVersion("baseVersion")
                .setSha1("sha1")
        );
    }

    @Test
    public void apply_getApplicationMetadataThrowsException_shouldThrow() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_GATEWAY, "test exception");
        given(nexusRepoMock.getApplicationMetadata()).willThrow(exception);

        assertThatThrownBy(() -> target.apply(applicationMock))
                .isInstanceOf(DeployActionException.class)
                .hasMessage("Error downloading file")
                .hasCause(exception);
    }

    @Test
    public void apply_receivesValidNexusResponse_shouldSetLatestVersion() {

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(nexusRepoMock).getApplicationMetadata();
        verify(applicationMock).setLatest(applicationMetadataArgumentCaptor.capture());

        ApplicationMetadata captorValue = applicationMetadataArgumentCaptor.getValue();

        assertThat(captorValue.getVersion()).isEqualTo("baseVersion");
        assertThat(captorValue.getRepositoryId()).isEqualTo("staging");
        assertThat(captorValue.getSha1()).isEqualTo("sha1");
        assertThat(captorValue.getFile()).isNull();
    }
}
