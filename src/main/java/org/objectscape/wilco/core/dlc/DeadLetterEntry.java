package org.objectscape.wilco.core.dlc;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by plohmann on 25.03.2015.
 */
public class DeadLetterEntry {

    final private String queueId;
    final private Throwable exception;
    final private String stackTrace;
    final private long creationTime = System.currentTimeMillis();

    public DeadLetterEntry(String queueId, Throwable exception) {
        this.queueId = queueId;
        this.exception = exception;
        if(exception != null) {
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString();
        }
        else {
            stackTrace = null;
        }
    }

    public String getQueueId() {
        return queueId;
    }

    public Throwable getException() {
        return exception;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
