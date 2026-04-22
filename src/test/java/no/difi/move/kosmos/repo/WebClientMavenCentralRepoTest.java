package no.difi.move.kosmos.repo;

import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.config.KosmosProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebClientMavenCentralRepoTest {

    @TempDir
    File tempFolder;

    private static MockWebServer mockWebServer;

    @Mock
    private KosmosProperties properties;

    @InjectMocks
    private WebClientMavenCentralRepo target;

    @BeforeEach
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        when(properties.getMavenCentral()).thenReturn(mockWebServer.url("/download").url());
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockWebServer.close();
    }

    @Test
    public void downloadJAR_BadRequest_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        final File download = new File(tempFolder, "test.jar");
        assertThatThrownBy(() -> target.downloadJAR("version", download.toPath()))
                .isInstanceOf(KosmosActionException.class);
    }

    @Test
    public void downloadJAR_InternalServerError_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        final File download = new File(tempFolder, "test.jar");
        assertThatThrownBy(() -> target.downloadJAR("version", download.toPath()))
                .isInstanceOf(KosmosActionException.class);
    }

    @Test
    public void downloadJAR_Success_FileShouldHaveExpectedContent() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                .setBody("jarcontent"));
        final File download = new File(tempFolder, "test.jar");

        target.downloadJAR("2.0.0-SNAPSHOT", download.toPath());

        assertThat(download).hasContent("jarcontent");
    }

    @Test
    public void getChecksum_BadRequest_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        assertThatThrownBy(() -> target.getChecksum("2.0.0-SNAPSHOT", "sha1"))
                .isInstanceOf(KosmosActionException.class);
    }

    @Test
    public void getChecksum_InternalServerError_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        assertThatThrownBy(() -> target.getChecksum("2.0.0-SNAPSHOT", "sha1"))
                .isInstanceOf(KosmosActionException.class);
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

    @Test
    public void downloadSignature_Success_SignatureShouldHaveExpectedContent() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("sigbody")
                .setResponseCode(200));
        assertThat(target.downloadSignature("2.2.0-SNAPSHOT"))
                .contains("sigbody");
    }

    @Test
    public void downloadSignature_BadRequest_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        assertThatThrownBy(() -> target.downloadSignature("version"))
                .isInstanceOf(KosmosActionException.class);
    }

    @Test
    public void downloadSignature_InternalServerError_ShouldThrow() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        assertThatThrownBy(() -> target.downloadSignature("version"))
                .isInstanceOf(KosmosActionException.class);
    }

}
