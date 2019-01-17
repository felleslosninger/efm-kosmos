package no.difi.move.deploymanager.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
public interface Command {

    void run(CommandLine commandLine, Options options);

    boolean supports(CommandLine cmd);

    void commandLine(Options options);
}
