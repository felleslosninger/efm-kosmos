package no.difi.move.deploymanager.command;

import lombok.RequiredArgsConstructor;
import no.difi.move.deploymanager.config.DeployManagerProperties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Component
@RequiredArgsConstructor
public class VerboseCommand implements Command {

    private final DeployManagerProperties properties;

    @Override
    public void run(CommandLine commandLine, Options options) {
        properties.setVerbose(true);
    }

    @Override
    public boolean supports(CommandLine cmd) {
        Assert.notNull(cmd, "cmd");
        return cmd.hasOption("v");
    }

    @Override
    public void commandLine(Options options) {
        Assert.notNull(options, "options");
        options.addOption("v", "verbose", false, "Follow application log.");
    }
}
