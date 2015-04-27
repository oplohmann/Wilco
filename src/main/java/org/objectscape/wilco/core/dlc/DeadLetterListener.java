package org.objectscape.wilco.core.dlc;

import java.util.function.Consumer;

/**
 * Created by plohmann on 25.03.2015.
 */
public class DeadLetterListener {

    final private String queueId;
    final private Class<? extends Throwable> exceptionClass;
    final private Consumer<DeadLetterEntry> callback;

    public DeadLetterListener(String queueId, Class<? extends Throwable> exceptionClass, Consumer<DeadLetterEntry> callback) {
        if(callback == null) {
            throw new NullPointerException("callback null");
        }
        this.queueId = queueId;
        this.exceptionClass = exceptionClass;
        this.callback = callback;
    }

    public String getQueueId() {
        return queueId;
    }

    public Class<? extends Throwable> getExceptionClass() {
        return exceptionClass;
    }

    public Consumer<DeadLetterEntry> getCallback() {
        return callback;
    }

    public boolean matches(DeadLetterEntry entry) {
        if(queueId != null && !queueId.equals(entry.getQueueId())) {
            return false;
        }
        if(exceptionClass != null && !exceptionClass.isInstance(entry.getException())) {
            return false;
        }
        return true;
    }
}
