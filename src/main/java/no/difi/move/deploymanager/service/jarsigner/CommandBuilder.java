package no.difi.move.deploymanager.service.jarsigner;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Setter
@Accessors(fluent = true)
class CommandBuilder {

    private final String jarPath;
    private String keystore;
    private String password;
    private String alias;

    List<String> build() {
        List<String> command = new ArrayList<>();
        command.add("jarsigner");
        command.add("-verify");

        if (keystore != null) {
            command.add("-keystore");
            command.add(keystore);
        }

        if (password != null) {
            command.add("-storepass");
            command.add(password);
        }

        command.add(jarPath);

        if (alias != null) {
            command.add(alias);
        }

        return command;
    }
}
