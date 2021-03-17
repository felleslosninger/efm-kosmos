package no.difi.move.deploymanager.cucumber;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.io.InputStream;

public class CucumberResourceLoader extends DefaultResourceLoader {

    public byte[] getResourceBytes(String location) throws IOException {
        try (InputStream is = super.getResource(location).getInputStream()) {
            return IOUtils.toByteArray(is);
        }
    }
}
