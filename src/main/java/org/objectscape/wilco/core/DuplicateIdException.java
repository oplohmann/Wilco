package org.objectscape.wilco.core;

/**
 * Created by plohmann on 03.03.2015.
 */
public class DuplicateIdException extends RuntimeException {

    public DuplicateIdException() {
    }

    public DuplicateIdException(String message) {
        super(message);
    }

    public DuplicateIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateIdException(Throwable cause) {
        super(cause);
    }

    public DuplicateIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
