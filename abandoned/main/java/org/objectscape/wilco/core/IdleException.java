package org.objectscape.wilco.core;

/**
 * Created by plohmann on 29.05.2015.
 */
public class IdleException extends RuntimeException {

    public IdleException() {
    }

    public IdleException(String message) {
        super(message);
    }

    public IdleException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdleException(Throwable cause) {
        super(cause);
    }

    public IdleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
