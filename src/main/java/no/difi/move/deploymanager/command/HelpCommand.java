package no.difi.move.deploymanager.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@Component
public class HelpCommand implements Command {

    @Override
    public void run(CommandLine commandLine, Options options) {
        Assert.notNull(options, "options");
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("deploymanager", options);
        System.exit(1);
    }

    @Override
    public boolean supports(CommandLine cmd) {
        Assert.notNull(cmd, "cmd");
        return cmd.hasOption("h");
    }

    @Override
    public void commandLine(Options options) {
        Assert.notNull(options, "options");
        options.addOption("h", "help", false, "Display help");
    }
}
