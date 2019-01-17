package no.difi.move.deploymanager.action.application;

import no.difi.move.deploymanager.action.DeployActionException;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.domain.application.Application;
import no.difi.move.deploymanager.domain.application.ApplicationMetadata;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LatestVersionAction.class, IOUtils.class})
public class LatestVersionActionTest {

    @Mock private URL urlMock;
    @Mock private URLConnection connectionMock;
    @Mock private DeployManagerProperties propertiesMock;
    @Mock private Application applicationMock;
    @Mock private InputStream streamMock;

    @Captor private ArgumentCaptor<ApplicationMetadata> applicationMetadataArgumentCaptor;

    @InjectMocks private LatestVersionAction target;

    @Before
    public void setUp() throws Exception {
        when(urlMock.openConnection()).thenReturn(connectionMock);
        whenNew(URL.class).withParameterTypes(String.class)
                .withArguments(Mockito.anyString()).thenReturn(urlMock);
        when(propertiesMock.getNexusProxyURL()).thenReturn(urlMock);
    }

    @Test(expected = NullPointerException.class)
    public void apply_toNull_shouldThrow() {
        target.apply(null);
    }

    @Test
    public void apply_openConnectionThrowsIOException_shouldThrow() throws IOException {
        IOException exception = new IOException("test exception");
        given(urlMock.openConnection()).willThrow(exception);

        assertThatThrownBy(() -> target.apply(applicationMock))
                .isInstanceOf(DeployActionException.class)
                .hasMessage("Error downloading file")
                .hasCause(exception);
    }

    @Test
    public void apply_receivesValidNexusResponse_shouldSetLatestVersion() throws IOException {
        final String nexusResponse = getNexusResponse();
        when(connectionMock.getInputStream()).thenReturn(streamMock);
        when(connectionMock.getContentEncoding()).thenReturn(null);
        when(propertiesMock.getRepository()).thenReturn("staging");
        mockStatic(IOUtils.class);
        when(IOUtils.toString(streamMock, connectionMock.getContentEncoding())).thenReturn(nexusResponse);

        assertThat(target.apply(applicationMock)).isSameAs(applicationMock);

        verify(applicationMock).setLatest(applicationMetadataArgumentCaptor.capture());

        ApplicationMetadata captorValue = applicationMetadataArgumentCaptor.getValue();

        assertThat(captorValue.getVersion()).isEqualTo("baseVersion");
        assertThat(captorValue.getRepositoryId()).isEqualTo("staging");
        assertThat(captorValue.getSha1()).isEqualTo("sha1");
        assertThat(captorValue.getFile()).isNull();
    }

    private String getNexusResponse() {
        return "{\n" +
                "  \"baseVersion\": \"baseVersion\",\n" +
                "  \"version\": \"version\",\n" +
                "  \"sha1\": \"sha1\",\n" +
                "  \"downloadUri\": \"https://downloadUri.here\"\n" +
                "}";
    }
}
