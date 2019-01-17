package no.difi.move.deploymanager.action;

/**
 * @author Nikolai Luthman <nikolai dot luthman at inmeta dot no>
 */
public class DeployActionException extends RuntimeException {

    public DeployActionException() {
    }

    public DeployActionException(String message) {
        super(message);
    }

    public DeployActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeployActionException(Throwable cause) {
        super(cause);
    }

    public DeployActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
