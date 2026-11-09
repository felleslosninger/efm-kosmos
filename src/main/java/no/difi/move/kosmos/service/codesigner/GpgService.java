package no.difi.move.kosmos.service.codesigner;

import java.io.InputStream;

public interface GpgService {
    boolean verify(String signedDataFilePath, String signatureFilePath);

    /**
     * Verifies the signature against the data read from the stream. The stream is read to the end, but not closed.
     */
    boolean verify(InputStream signedData, String signature);
}
