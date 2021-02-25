package no.difi.move.deploymanager.service.jarsigner;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class JarsSignerServiceImpl implements JarsSignerService {

    private final DeployManagerProperties properties;

    @Override
    @SneakyThrows({IOException.class, InterruptedException.class, TimeoutException.class, InvalidExitValueException.class})
    public void verify(String path) {
        new ProcessExecutor(getCommand(path))
                .directory(new File(properties.getHome()))
                .redirectOutput(Slf4jStream.ofCaller().asInfo())
                .exitValues(0, 16) /* Exit value 16 accepts unsigned JARs */
                .execute();
    }

    private List<String> getCommand(String path) {
        CommandBuilder commandBuilder = new CommandBuilder(path);

        Optional.ofNullable(properties.getKeystore()).ifPresent(keystore ->
                commandBuilder
                        .keystore(keystore.getPath())
                        .password(keystore.getPassword())
                        .alias(keystore.getAlias())
        );

        return commandBuilder.build();
    }
}
