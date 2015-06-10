package org.objectscape.wilco.core;

/**
 * Created by plohmann on 25.03.2015.
 */
public class QueueClosedException extends RuntimeException {

    public QueueClosedException() {
    }

    public QueueClosedException(String message) {
        super(message);
    }

    public QueueClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueueClosedException(Throwable cause) {
        super(cause);
    }

    public QueueClosedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
