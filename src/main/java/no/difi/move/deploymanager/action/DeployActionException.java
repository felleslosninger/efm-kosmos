package no.difi.move.deploymanager.action;

public class DeployActionException extends RuntimeException {

    public DeployActionException(String message) {
        super(message);
    }

    public DeployActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
