package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.util.QueueAnchorPair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nutzer on 15.05.2015.
 */
public class ShutdownSchedulerTask extends ShutdownTask {

    final private CompletableFuture<List<Runnable>> doneSignal;
    final private List<QueueAnchorPair> nonEmptyQueues;

    public ShutdownSchedulerTask(WilcoCore core, CompletableFuture<List<Runnable>> doneSignal, long duration, TimeUnit unit, long start, List<QueueAnchorPair> nonEmptyQueues) {
        super(core, doneSignal, duration, unit, start);
        this.doneSignal = doneSignal;
        this.nonEmptyQueues = nonEmptyQueues;
    }

    @Override
    public boolean run(Context context) {
        List<Runnable> numberOfRunningTasks = shutdownThreadPoolAndScheduler(context);
        doneSignal.complete(numberOfRunningTasks);
        return false; // returning false will make the Scheduler exit its main loop
    }

    @Override
    public int priority() {
        return MAX_PRIORITY;
    }

    private List<Runnable> shutdownThreadPoolAndScheduler(Context context) {
        long remainingShutdownTime = unit.toMillis(duration) - (System.currentTimeMillis() - start);
        if(remainingShutdownTime > 0) {
            context.getExecutor().shutdown();
            try {
                context.getExecutor().awaitTermination(remainingShutdownTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                context.addToDeadLetterQueue(null, e);
            }
        } else {
            context.getExecutor().shutdownNow();
        }
        core.commitShutdown();
        return new ArrayList<>(getStillRunningRunnables());
    }

    public List<Runnable> getStillRunningRunnables() {
        // TODO - way to do this using flatMap?
        List<Runnable> stillRunningRunnables = new ArrayList<>();
        for(QueueAnchorPair pair : nonEmptyQueues) {
            stillRunningRunnables.addAll(pair.getUserRunnables());
        }
        return stillRunningRunnables;
    }
}
