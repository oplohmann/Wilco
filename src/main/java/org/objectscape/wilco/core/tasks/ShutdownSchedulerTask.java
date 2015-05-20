package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.Queue;
import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.ShutdownResponse;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.util.QueueAnchorPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Nutzer on 15.05.2015.
 */
public class ShutdownSchedulerTask extends ShutdownTask {

    final private CompletableFuture<ShutdownResponse> doneSignal;
    final private List<QueueAnchorPair> nonEmptyQueues;

    public ShutdownSchedulerTask(WilcoCore core, CompletableFuture<ShutdownResponse> doneSignal, long duration, TimeUnit unit, long start, List<QueueAnchorPair> nonEmptyQueues) {
        super(core, doneSignal, duration, unit, start);
        this.doneSignal = doneSignal;
        this.nonEmptyQueues = nonEmptyQueues;
    }

    @Override
    public boolean run(Context context) {
        ShutdownResponse numberOfRunningTasks = shutdownThreadPoolAndScheduler(context);
        doneSignal.complete(numberOfRunningTasks);
        return false; // returning false will make the Scheduler exit its main loop
    }

    @Override
    public int priority() {
        return MAX_PRIORITY;
    }

    private ShutdownResponse shutdownThreadPoolAndScheduler(Context context) {
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
        return createShutdownResponse();
    }

    private ShutdownResponse createShutdownResponse() {
        Map<Queue, List<Runnable>> notCompletedRunnablesByQueue = new HashMap<>();
        for(QueueAnchorPair queueAnchorPair : nonEmptyQueues) {
            List<Runnable> notCompletedRunnables = notCompletedRunnablesByQueue.get(queueAnchorPair.getQueue());
            if(notCompletedRunnables == null) {
                notCompletedRunnables = new ArrayList<>();
                notCompletedRunnablesByQueue.put(queueAnchorPair.getQueue(), notCompletedRunnables);
            }
            notCompletedRunnables.addAll(queueAnchorPair.getUserRunnables());
        }
        return new ShutdownResponse(notCompletedRunnablesByQueue);
    }

    public List<Runnable> getStillRunningRunnables() {
        return nonEmptyQueues.stream().flatMap(queueAnchorPair -> queueAnchorPair.getUserRunnables().stream()).collect(Collectors.toList());
    }
}
