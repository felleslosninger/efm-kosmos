package no.difi.move.deploymanager.config;

import lombok.experimental.UtilityClass;
import no.difi.move.deploymanager.command.Command;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
@UtilityClass
public final class CommandLineOptions {

    public static Options options(List<Command> commands) {
        Options options = new Options();

        for (Command command : commands) {
            command.commandLine(options);
        }

        return options;
    }
}
