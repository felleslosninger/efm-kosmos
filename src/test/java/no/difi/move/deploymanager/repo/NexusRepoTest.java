package no.difi.move.deploymanager.repo;

import lombok.SneakyThrows;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class NexusRepoTest {
    @Mock private DeployManagerProperties properties;
    @InjectMocks private NexusRepo nexusRepo;

    @Before
    @SneakyThrows(MalformedURLException.class)
    public void before() {
        given(properties.getNexus()).willReturn(new URL("https://nexus.difi.no"));
        given(properties.getRepository()).willReturn("getRepositoryResponse");
        given(properties.getGroupId()).willReturn("getGroupIdResponse");
        given(properties.getArtifactId()).willReturn("getArtifactIdResponse");
    }

    @Test
    public void testGetArtifact() throws Exception {
        assertThat(
                nexusRepo.getArtifact("version", "classifier")
        ).isEqualTo(new URL("https://nexus.difi.no/service/local/artifact/maven/content?r=getRepositoryResponse&g=getGroupIdResponse&a=getArtifactIdResponse&v=version&e=classifier"));
    }

    @Test
    public void testNoClassifier() throws Exception {
        assertThat(
                nexusRepo.getArtifact("version", null)
        ).isEqualTo(new URL("https://nexus.difi.no/service/local/artifact/maven/content?r=getRepositoryResponse&g=getGroupIdResponse&a=getArtifactIdResponse&v=version"));
    }
}
