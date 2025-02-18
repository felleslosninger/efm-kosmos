package no.difi.move.kosmos.service.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RefreshServiceImplTest {

    @InjectMocks
    private RefreshServiceImpl target;

    // FIXME denne må skrives om til å verifisere den nye refresh mekanismen

//    @Mock
//    private ConfigDataContextRefresher contextRefresher;

    @Test
    public void refresh_ContextRefresherShouldBeInvoked() {
        target.refreshConfig();
//        verify(contextRefresher).refresh();
    }

}