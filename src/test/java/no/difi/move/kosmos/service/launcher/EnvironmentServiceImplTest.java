package no.difi.move.kosmos.service.launcher;

import no.difi.move.kosmos.config.EnvironmentProperties;
import no.difi.move.kosmos.config.KosmosProperties;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnvironmentServiceImplTest {

    @InjectMocks
    private EnvironmentServiceImpl target;

    @Mock
    private KosmosProperties properties;

    private static final Map<String, String> MOCK_ENVIRONMENT = Map.of(
        "prefix1.name.here", "value1",
        "prefix2.name.here", "value2"
    );

    @Test
    public void getChildProcessEnvironment_NoExclusions_ChildAndParentEnvShouldBeEqual() {

        EnvironmentProperties environmentProperties = mock(EnvironmentProperties.class);
        when(environmentProperties.getPrefixesRemovedFromChildProcess()).thenReturn(Lists.newArrayList());
        when(properties.getEnvironment()).thenReturn(environmentProperties);

        Map<String, String> result = target.calculateFilteredChildProcessEnvironment(new HashMap<>(MOCK_ENVIRONMENT));

        assertEquals(MOCK_ENVIRONMENT, result);
        assertEquals("value1", result.get("prefix1.name.here"));
        assertEquals("value2", result.get("prefix2.name.here"));

    }

    @Test
    public void getChildProcessEnvironment_OnePrefixExcluded_ChildAndParentEnvShouldBeDifferent() {

        EnvironmentProperties environmentProperties = mock(EnvironmentProperties.class);
        when(environmentProperties.getPrefixesRemovedFromChildProcess()).thenReturn(Lists.newArrayList("prefix1"));
        when(properties.getEnvironment()).thenReturn(environmentProperties);

        Map<String, String> result = target.calculateFilteredChildProcessEnvironment(new HashMap<>(MOCK_ENVIRONMENT));

        assertNotEquals(MOCK_ENVIRONMENT, result);
        assertNull(result.get("prefix1.name.here"));
        assertEquals("value2", result.get("prefix2.name.here"));

    }

}