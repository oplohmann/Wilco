package org.objectscape.wilco.core;

/**
 * Created by Nutzer on 12.04.2015.
 */
public class AlreadySetException extends RuntimeException {

    public AlreadySetException() {
    }

    public AlreadySetException(String message) {
        super(message);
    }

    public AlreadySetException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadySetException(Throwable cause) {
        super(cause);
    }

    public AlreadySetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
