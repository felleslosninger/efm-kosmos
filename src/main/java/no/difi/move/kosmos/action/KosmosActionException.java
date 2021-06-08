package no.difi.move.kosmos.action;

public class KosmosActionException extends RuntimeException {

    public KosmosActionException(String message) {
        super(message);
    }

    public KosmosActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
