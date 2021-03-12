package no.difi.move.deploymanager.service.codesigner;

public interface GpgService {

    boolean verify(String signedDataFilePath, String signatureFilePath, String publicKeyFilePath);
}
