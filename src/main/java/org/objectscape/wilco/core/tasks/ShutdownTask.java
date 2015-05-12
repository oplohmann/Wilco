package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.WilcoCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 19.02.2015.
 */
public class ShutdownTask extends CoreTask {

    final private static Logger LOG = LoggerFactory.getLogger(ShutdownTask.class);

    private String wilco;
    private WilcoCore core;
    private Future<Void> doneSignal;
    final private long duration;
    final private TimeUnit unit;

    public ShutdownTask(String wilco, WilcoCore core, CompletableFuture<Void> doneSignal, int duration, TimeUnit unit) {
        super();
        this.wilco = wilco;
        this.doneSignal = doneSignal;
        this.duration = duration;
        this.unit = unit;
    }

    @Override
    public boolean run(Context context) {
        long start = System.currentTimeMillis();
        core.shutdown(start, Math.round(duration * 0.75), unit);
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
