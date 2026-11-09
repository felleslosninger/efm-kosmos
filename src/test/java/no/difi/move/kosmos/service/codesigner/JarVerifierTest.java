package no.difi.move.kosmos.service.codesigner;

import no.difi.move.kosmos.action.KosmosActionException;
import no.difi.move.kosmos.repo.JavaArchiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class JarVerifierTest {

    private static final String VERSION = "2.0.0";
    private static final String SIGNATURE = "the signature";
    private static final String CONTENT = "the jar content";

    @Mock
    private JavaArchiveRepository javaArchiveRepository;
    @Mock
    private GpgService gpgService;

    @InjectMocks
    private JarVerifier target;

    @TempDir
    Path tempDir;

    private File jarFile;

    @BeforeEach
    void setUp() throws Exception {
        jarFile = Files.writeString(tempDir.resolve("integrasjonspunkt-2.0.0.jar"), CONTENT).toFile();
    }

    @Test
    void verify_validSignature_returnsDigestOfVerifiedContent() throws Exception {
        given(javaArchiveRepository.downloadSignature(VERSION)).willReturn(SIGNATURE);
        given(gpgService.verify(any(InputStream.class), eq(SIGNATURE))).willAnswer(invocation -> {
            // read like the real verification does
            invocation.getArgument(0, InputStream.class).transferTo(OutputStream.nullOutputStream());
            return true;
        });

        byte[] digest = target.verify(VERSION, jarFile);

        assertThat(digest).isEqualTo(sha256(CONTENT));
    }

    @Test
    void verify_invalidSignature_throws() {
        given(javaArchiveRepository.downloadSignature(VERSION)).willReturn(SIGNATURE);
        given(gpgService.verify(any(InputStream.class), eq(SIGNATURE))).willReturn(false);

        assertThatThrownBy(() -> target.verify(VERSION, jarFile))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("Invalid signature for %s, version %s".formatted(jarFile, VERSION));
    }

    @Test
    void verify_signatureCannotBeDownloaded_throws() {
        KosmosActionException downloadFailure = new KosmosActionException("Signature fetch failed");
        given(javaArchiveRepository.downloadSignature(VERSION)).willThrow(downloadFailure);

        assertThatThrownBy(() -> target.verify(VERSION, jarFile))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("Could not verify %s for version %s".formatted(jarFile, VERSION))
                .hasCause(downloadFailure);
        verifyNoInteractions(gpgService);
    }

    @Test
    void verify_signerKeyNotTrusted_throws() {
        given(javaArchiveRepository.downloadSignature(VERSION)).willReturn(SIGNATURE);
        KosmosActionException keyFailure = new KosmosActionException("Signer public key not found in keyring");
        given(gpgService.verify(any(InputStream.class), eq(SIGNATURE))).willThrow(keyFailure);

        assertThatThrownBy(() -> target.verify(VERSION, jarFile))
                .isInstanceOf(KosmosActionException.class)
                .hasCause(keyFailure);
    }

    @Test
    void verify_jarIsMissing_throws() {
        given(javaArchiveRepository.downloadSignature(VERSION)).willReturn(SIGNATURE);
        File missing = tempDir.resolve("missing.jar").toFile();

        assertThatThrownBy(() -> target.verify(VERSION, missing))
                .isInstanceOf(KosmosActionException.class)
                .hasMessage("Could not verify %s for version %s".formatted(missing, VERSION));
    }

    @Test
    void isVerified_validSignature_returnsTrue() {
        given(javaArchiveRepository.downloadSignature(VERSION)).willReturn(SIGNATURE);
        given(gpgService.verify(any(InputStream.class), eq(SIGNATURE))).willReturn(true);

        assertThat(target.isVerified(VERSION, jarFile)).isTrue();
    }

    @Test
    void isVerified_invalidSignature_returnsFalse() {
        given(javaArchiveRepository.downloadSignature(VERSION)).willReturn(SIGNATURE);
        given(gpgService.verify(any(InputStream.class), eq(SIGNATURE))).willReturn(false);

        assertThat(target.isVerified(VERSION, jarFile)).isFalse();
    }

    @Test
    void isUnchanged_sameContent_returnsTrue() throws Exception {
        assertThat(target.isUnchanged(jarFile, sha256(CONTENT))).isTrue();
    }

    @Test
    void isUnchanged_changedContent_returnsFalse() throws Exception {
        byte[] digest = sha256(CONTENT);
        Files.writeString(jarFile.toPath(), "other content");

        assertThat(target.isUnchanged(jarFile, digest)).isFalse();
    }

    @Test
    void isUnchanged_fileRemoved_returnsFalse() throws Exception {
        byte[] digest = sha256(CONTENT);
        Files.delete(jarFile.toPath());

        assertThat(target.isUnchanged(jarFile, digest)).isFalse();
    }

    @Test
    void describe_includesCauses() {
        Exception exception = new KosmosActionException("Could not verify", new IllegalStateException("Signature fetch failed"));

        assertThat(JarVerifier.describe(exception)).isEqualTo("Could not verify: Signature fetch failed");
    }

    private static byte[] sha256(String content) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
    }
}
