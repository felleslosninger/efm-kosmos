package no.difi.move.deploymanager.service.codesigner;

import no.difi.move.deploymanager.action.DeployActionException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PublicKeyVerifierImplTest {

    private PGPPublicKey keyMock;
    private PublicKeyVerifierImpl target;

    @Before
    public void setUp() throws Exception {
        target = new PublicKeyVerifierImpl();
        keyMock = mock(PGPPublicKey.class);
        when(keyMock.getCreationTime()).thenReturn(Date.from(Instant.parse("2007-12-03T10:15:30.00Z")));
    }

    @Test
    public void verify_KeyIsExpired_ShouldThrow() {
        when(keyMock.getValidSeconds()).thenReturn(2L);
        assertThatThrownBy(() -> target.verify(keyMock))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void verify_KeyHasNoExpiry_ShouldPass() {
        when(keyMock.getValidSeconds()).thenReturn(0L);
        assertThatCode(() -> target.verify(keyMock)).doesNotThrowAnyException();
    }

    @Test
    public void verify_KeyIsNotExpired_ShouldPass() {
        when(keyMock.getCreationTime()).thenReturn(Date.from(Instant.now()));
        when(keyMock.getValidSeconds()).thenReturn(2000L);
        assertThatCode(() -> target.verify(keyMock)).doesNotThrowAnyException();
    }
}