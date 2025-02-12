package no.difi.move.kosmos.service.launcher;

import no.difi.move.kosmos.config.EnvironmentProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled("Not possible message encountered")
@ExtendWith(MockitoExtension.class)
public class EnvironmentServiceImplTest {

    @InjectMocks
    private EnvironmentServiceImpl target;

    @Mock
    private KosmosProperties properties;
    private static final Map<String, String> MOCK_ENVIRONMENT = new HashMap<String, String>() {{
        put("prefix1.name.here", "value1");
        put("prefix2.name.here", "value2");
    }};

    @BeforeEach
    public void setUp() {
        try (MockedStatic<System> mockSystem = mockStatic(System.class)) {
            // FIXME Mockito forbids mocking the static methods of System (and Thread).
            mockSystem.when(System::getenv).thenReturn(MOCK_ENVIRONMENT);
        }
    }

    @Test
    public void getChildProcessEnvironment_NoExclusions_ChildAndParentEnvShouldBeEqual() {
        EnvironmentProperties environmentProperties = mock(EnvironmentProperties.class);
        when(environmentProperties.getPrefixesRemovedFromChildProcess())
                .thenReturn(Lists.newArrayList());
        when(properties.getEnvironment()).thenReturn(environmentProperties);

        Map<String, String> result = target.getChildProcessEnvironment();

        assertEquals(MOCK_ENVIRONMENT, result);
    }

    @Test
    public void getChildProcessEnvironment_OnePrefixExcluded_ChildAndParentEnvShouldBeDifferent() {
        EnvironmentProperties environmentProperties = mock(EnvironmentProperties.class);
        when(environmentProperties.getPrefixesRemovedFromChildProcess())
                .thenReturn(Lists.newArrayList("prefix1"));
        when(properties.getEnvironment()).thenReturn(environmentProperties);

        Map<String, String> result = target.getChildProcessEnvironment();

        assertNotEquals(MOCK_ENVIRONMENT, result);
        assertNull(result.get("prefix1.name.here"));
        assertEquals("value2", result.get("prefix2.name.here"));
    }
}