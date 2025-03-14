package no.difi.move.kosmos.service.codesigner;

import no.difi.move.kosmos.action.KosmosActionException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// FIXME Test works when run manually separately with with : mvn -Dtest=PublicKeyVerifierImplTest clean test
@Disabled("Caused by: java.lang.SecurityException: digest missing for org/bouncycastle/openpgp/PGPPublicKey.class")
@ExtendWith(MockitoExtension.class)
public class PublicKeyVerifierImplTest {

    private PublicKeyVerifierImpl target;

    @BeforeEach
    public void setUp() {
        target = new PublicKeyVerifierImpl();
    }

    @Test
    public void verify_KeyIsExpired_ShouldThrow() {
        PGPPublicKey keyMock = mock(PGPPublicKey.class);
        when(keyMock.getValidSeconds()).thenReturn(2L);
        when(keyMock.getCreationTime()).thenReturn(Date.from(Instant.parse("2007-12-03T10:15:30.00Z")));
        assertThrows(KosmosActionException.class, () -> target.verify(keyMock));
    }

    @Test
    public void verify_KeyHasNoExpiry_ShouldPass() {
        PGPPublicKey keyMock = mock(PGPPublicKey.class);
        when(keyMock.getValidSeconds()).thenReturn(0L);
        assertDoesNotThrow(() -> target.verify(keyMock));
    }

    @Test
    public void verify_KeyIsNotExpired_ShouldPass() {
        PGPPublicKey keyMock = mock(PGPPublicKey.class);
        when(keyMock.getCreationTime()).thenReturn(Date.from(Instant.now()));
        when(keyMock.getValidSeconds()).thenReturn(2000L);
        assertDoesNotThrow(() -> target.verify(keyMock));
    }

}