package org.objectscape.wilco.core;

/**
 * Created by plohmann on 12.05.2015.
 */
public class ShutdownException extends RuntimeException {

    public ShutdownException() {
    }

    public ShutdownException(String message) {
        super(message);
    }

    public ShutdownException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShutdownException(Throwable cause) {
        super(cause);
    }

    public ShutdownException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
