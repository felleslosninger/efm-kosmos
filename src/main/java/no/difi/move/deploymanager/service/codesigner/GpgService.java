package no.difi.move.deploymanager.service.codesigner;

import java.util.List;

public interface GpgService {

    boolean verify(String signedDataFilePath, String signatureFilePath, List<String> publicKeys);
}
