package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 19.02.2015.
 */
public class ShutdownTask extends CoreTask {

    final private Future<Void> doneSignal;
    final private long duration;
    final private TimeUnit unit;

    public ShutdownTask(Future<Void> doneSignal, long duration, TimeUnit unit) {
        super();
        this.doneSignal = doneSignal;
        this.duration = duration;
        this.unit = unit;
    }

    @Override
    public boolean run(Context context) {
        context.getExecutor().shutdown();
        try {
            if(duration > 0) {
                context.getExecutor().awaitTermination(duration, unit);
            }
        } catch (InterruptedException e) {
            context.addToDeadLetterQueue(null, e);
        }
        return false;
    }

    @Override
    public int priority() {
        return MAX_PRIORITY;
    }
}
