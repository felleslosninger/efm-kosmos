package no.difi.move.deploymanager.command;

import lombok.RequiredArgsConstructor;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.springframework.stereotype.Component;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Component
@RequiredArgsConstructor
public class EnvironmentCommand implements Command {

    private final DeployManagerProperties properties;

    @Override
    public void run(CommandLine commandLine, Options options) {
        properties.setEnvironment(commandLine.getOptionValue("e"));
    }

    @Override
    public boolean supports(CommandLine cmd) {
        return cmd.hasOption("e");
    }

    @Override
    public void commandLine(Options options) {
        options.addOption("e", "environment", true, "Add environment variables to application");
    }
}
