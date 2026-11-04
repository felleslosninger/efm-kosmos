package no.difi.move.kosmos.handler;

import no.difi.move.kosmos.action.application.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SynchronizationHandlerTest {

    @InjectMocks
    private SynchronizationHandler target;

    @Mock
    private GetCurrentVersionAction currentVersionAction;
    @Mock
    private LatestVersionAction latestVersionAction;
    @Mock
    private VerifyVersionComplianceAction verifyVersionComplianceAction;
    @Mock
    private PrepareApplicationAction prepareApplicationAction;
    @Mock
    private ValidateAction validateAction;
    @Mock
    private ShutdownAction shutdownAction;
    @Mock
    private StartAction startAction;
    @Mock
    private RollbackAction rollbackAction;

    @Test
    public void run_overlappingRuns_shouldRunOneAtATime() throws Exception {
        CountDownLatch firstRunStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstRun = new CountDownLatch(1);
        doCallRealMethod().when(currentVersionAction).andThen(any());
        doAnswer(invocation -> {
            firstRunStarted.countDown();
            assertThat(releaseFirstRun.await(10, TimeUnit.SECONDS)).isTrue();
            return invocation.getArgument(0);
        }).doAnswer(invocation -> invocation.getArgument(0))
                .when(currentVersionAction).apply(any());

        CompletableFuture<Void> firstRun = CompletableFuture.runAsync(target::run);
        assertThat(firstRunStarted.await(10, TimeUnit.SECONDS)).isTrue();
        CompletableFuture<Void> secondRun = CompletableFuture.runAsync(target::run);

        verify(currentVersionAction, after(500).times(1)).apply(any());
        assertThat(secondRun).isNotDone();

        releaseFirstRun.countDown();
        CompletableFuture.allOf(firstRun, secondRun).get(10, TimeUnit.SECONDS);
        verify(currentVersionAction, times(2)).apply(any());
        verify(rollbackAction, times(2)).apply(any());
    }
}
