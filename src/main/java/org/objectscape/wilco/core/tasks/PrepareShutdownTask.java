package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.ShutdownResponse;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.util.QueueAnchorPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 19.02.2015.
 */
public class PrepareShutdownTask extends ShutdownTask {

    final private static Logger LOG = LoggerFactory.getLogger(PrepareShutdownTask.class);

    private String wilco;

    public PrepareShutdownTask(String wilco, WilcoCore core, CompletableFuture<ShutdownResponse> doneSignal, long duration, TimeUnit unit) {
        super(core, doneSignal, duration, unit, System.currentTimeMillis());
        this.wilco = wilco;
    }

    @Override
    public boolean run(Context context) {
        List<QueueAnchorPair> allQueues = core.closeAllQueues();
        core.scheduleAdminTask(new RunEmptyShutdownTask(core, doneSignal, duration, unit, start, allQueues));
        return true;
    }

    @Override
    public int priority() {
        // MAX_PRIORITY because it is important to close down all queues asap for the shutdown process to be swift
        return MAX_PRIORITY;
    }
}
