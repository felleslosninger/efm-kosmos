package no.difi.move.kosmos.service.codesigner;

public interface GpgService {
    boolean verify(String signedDataFilePath, String signatureFilePath);
}
