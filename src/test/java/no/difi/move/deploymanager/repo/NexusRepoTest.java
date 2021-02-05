package no.difi.move.deploymanager.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@RestClientTest(NexusRepo.class)
@ActiveProfiles("test")
public class NexusRepoTest {

    @ClassRule
    public static final TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

    @Autowired
    private NexusRepo nexusRepo;
    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDownloadJAR() {
        server.expect(requestTo("https://beta-meldingsutveksling.difi.no/service/local/artifact/maven/content?r=staging&g=no.difi.meldingsutveksling&a=integrasjonspunkt&v=1.7.93-SNAPSHOT"))
                .andRespond(withSuccess("jarcontent", MediaType.APPLICATION_OCTET_STREAM));

        File destination = new File(TEMP_FOLDER.getRoot(), "test.jar");
        nexusRepo.downloadJAR("1.7.93-SNAPSHOT", destination.toPath());

        assertThat(destination).hasContent("jarcontent");
    }

    @Test
    public void testGetChecksum() {
        server.expect(requestTo("https://beta-meldingsutveksling.difi.no/service/local/artifact/maven/content?r=staging&g=no.difi.meldingsutveksling&a=integrasjonspunkt&v=1.7.93-SNAPSHOT&e=sha1"))
                .andRespond(withSuccess("414243", MediaType.APPLICATION_JSON));

        assertThat(nexusRepo.getChecksum("1.7.93-SNAPSHOT", "sha1"))
                .containsExactly(65, 66, 67);
    }

    @SneakyThrows(JsonProcessingException.class)
    private String json(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
