package no.difi.move.deploymanager.service.jarsigner;

import lombok.SneakyThrows;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import no.difi.move.deploymanager.config.KeystoreProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.util.List;

import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JarsSignerServiceImpl.class})
public class JarsSignerServiceImplTest {
    @SuppressWarnings("unused")
    @Spy
    private DeployManagerProperties properties = new DeployManagerProperties()
            .setRoot("/tmp/root")
            .setKeystore(new KeystoreProperties()
                    .setPath("/keystore/keystore.jks")
                    .setPassword("xxx")
                    .setAlias("stuntman")
            );

    @Mock private CommandBuilder commandBuilderMock;
    @Mock private List<String> commandMock;
    @Mock private ProcessExecutor processExecutorMock;
    @Mock private File fileMock;

    @InjectMocks private JarsSignerServiceImpl target;

    @Test
    @SneakyThrows
    public void testVerify() {
        whenNew(CommandBuilder.class).withAnyArguments().thenReturn(commandBuilderMock);
        given(commandBuilderMock.keystore(any())).willAnswer(RETURNS_SELF);
        given(commandBuilderMock.password(any())).willAnswer(RETURNS_SELF);
        given(commandBuilderMock.alias(any())).willAnswer(RETURNS_SELF);
        given(commandBuilderMock.build()).willReturn(commandMock);

        whenNew(ProcessExecutor.class).withAnyArguments().thenReturn(processExecutorMock);
        given(processExecutorMock.directory(any())).willAnswer(RETURNS_SELF);
        given(processExecutorMock.redirectOutput(any())).willAnswer(RETURNS_SELF);
        given(processExecutorMock.exitValueNormal()).willAnswer(RETURNS_SELF);

        whenNew(File.class).withAnyArguments().thenReturn(fileMock);

        target.verify("/tmp/root/integrasjonspunkt-1.7.93.jar");

        verifyNew(CommandBuilder.class).withArguments("/tmp/root/integrasjonspunkt-1.7.93.jar");
        verify(commandBuilderMock).keystore("/keystore/keystore.jks");
        verify(commandBuilderMock).password("xxx");
        verify(commandBuilderMock).alias("stuntman");
        verify(commandBuilderMock).build();

        verifyNew(ProcessExecutor.class).withArguments(same(commandMock));
        verify(processExecutorMock).directory(same(fileMock));
        verify(processExecutorMock).redirectOutput(any());
        verify(processExecutorMock).exitValueNormal();
        verify(processExecutorMock).execute();
    }
}
