package no.difi.move.deploymanager.action;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
public class DeployActionException extends RuntimeException {

    public DeployActionException(String message) {
        super(message);
    }

    public DeployActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
