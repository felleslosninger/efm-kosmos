package no.difi.move.deploymanager.repo;

import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebClientNexusRepoTest {

    @ClassRule
    public static final TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

    private static MockWebServer mockWebServer;

    @Mock
    private DeployManagerProperties properties;

    @InjectMocks
    private WebClientNexusRepo target;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        when(properties.getNexus()).thenReturn(mockWebServer.url("/download").url());
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.close();
    }

    @Test
    public void downloadJAR_BadRequest_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        final File download = new File(TEMP_FOLDER.getRoot(), "test.jar");
        assertThatThrownBy(() -> target.downloadJAR("version", download.toPath()))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void downloadJAR_InternalServerError_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        final File download = new File(TEMP_FOLDER.getRoot(), "test.jar");
        assertThatThrownBy(() -> target.downloadJAR("version", download.toPath()))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void downloadJAR_Success_FileShouldHaveExpectedContent() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                .setBody("jarcontent"));
        final File download = new File(TEMP_FOLDER.getRoot(), "test.jar");

        target.downloadJAR("2.0.0-SNAPSHOT", download.toPath());

        assertThat(download).hasContent("jarcontent");
    }

    @Test
    public void getChecksum_BadRequest_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        assertThatThrownBy(() -> target.getChecksum("2.0.0-SNAPSHOT", "sha1"))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void getChecksum_InternalServerError_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        assertThatThrownBy(() -> target.getChecksum("2.0.0-SNAPSHOT", "sha1"))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void getChecksum_Success_ChecksumShouldHaveExpectedContent() {
        mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .setBody("414243"));
        assertThat(target.getChecksum("2.0.0-SNAPSHOT", "sha1"))
                .containsExactly(65, 66, 67);
    }
}