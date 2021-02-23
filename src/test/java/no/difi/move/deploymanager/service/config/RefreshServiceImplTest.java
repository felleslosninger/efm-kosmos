package no.difi.move.deploymanager.service.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.refresh.ContextRefresher;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RefreshServiceImplTest {

    @InjectMocks
    private RefreshServiceImpl target;

    @Mock
    private ContextRefresher contextRefresher;

    @Test
    public void refresh_ContextRefresherShouldBeInvoked() {
        target.refreshConfig();
        verify(contextRefresher).refresh();
    }
}