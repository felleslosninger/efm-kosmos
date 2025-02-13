package no.difi.move.kosmos.service.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.context.refresh.ConfigDataContextRefresher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RefreshServiceImplTest {

    @InjectMocks
    private RefreshServiceImpl target;

    @Mock
    private ConfigDataContextRefresher contextRefresher;

    @Test
    public void refresh_ContextRefresherShouldBeInvoked() {
        target.refreshConfig();
        verify(contextRefresher).refresh();
    }

}