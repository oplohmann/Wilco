package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.util.QueueAnchorPair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Nutzer on 18.05.2015.
 */
public class RunEmptyShutdownTask extends ShutdownTask {

    final private List<QueueAnchorPair> allQueues;

    public RunEmptyShutdownTask(WilcoCore core, CompletableFuture<List<Runnable>> doneSignal, long duration, TimeUnit unit, long start, List<QueueAnchorPair> allQueues) {
        super(core, doneSignal, duration, unit, start);
        this.allQueues = allQueues;
    }

    @Override
    public boolean run(Context context) {
        List<QueueAnchorPair> nonEmptyQueues = allQueues.stream().filter(queueAnchor -> !queueAnchor.getQueue().isEmpty()).collect(Collectors.toList());
        if(nonEmptyQueues.isEmpty()) {
            scheduleShutdown(nonEmptyQueues);
            return true;
        }
        waitTilRunEmptyThenShutdown(context, nonEmptyQueues);
        return true;
    }

    @Override
    public int priority() {
        // This means that the RunEmptyShutdownTask won't be executed until there is no single item
        // left in the queues with Priority CoreTask.MAX_PRIORITY nor CoreTask.MEDIUM_PRIORITY,
        // which means that all other queues have run empty and it is save to shut down the scheduler
        // now as no pending tasks can be skipped.
        return CoreTask.MIN_PRIORITY;
    }

    private void scheduleShutdown(List<QueueAnchorPair> nonEmptyQueues) {
        core.scheduleAdminTask(new ShutdownSchedulerTask(core, doneSignal, duration, unit, start, nonEmptyQueues));
    }

    private void waitTilRunEmptyThenShutdown(Context context, List<QueueAnchorPair> nonEmptyQueues) {
        // This tasks currently blocks the Scheduler and therefore pending tasks cannot be dispatched.
        // For the thread pool to be able to run empty, this tasks needs to release the Scheduler.
        // For that purpose waiting for all queues to run empty before shutting down the Scheduler and the thread
        // pool is done in a separate thread by invoking the thread pool executor as in the code below.
        context.getExecutor().execute(()-> {
            long durationInMillis = unit.toMillis(duration);
            boolean proceed = true;
            while (proceed) {
                List<QueueAnchorPair> currentNonEmptyQueues = new ArrayList<>(nonEmptyQueues);
                for(QueueAnchorPair queueAnchorPair : currentNonEmptyQueues) {
                    assert queueAnchorPair.getQueue().isClosed();
                    if(queueAnchorPair.getQueue().isEmpty()) {
                        nonEmptyQueues.remove(queueAnchorPair);
                    }
                }
                if(!nonEmptyQueues.isEmpty()) {
                    try {
                        long remainingShutdownTime = durationInMillis - (System.currentTimeMillis() - start);
                        if(remainingShutdownTime < 50) {
                            proceed = false;
                        }
                        else {
                            long sleepTime = remainingShutdownTime >= 100 ? 100 : 50;
                            Thread.sleep(sleepTime);
                        }
                    } catch (InterruptedException e) { }
                }
                else {
                    proceed = false;
                }
            }
            scheduleShutdown(nonEmptyQueues);
        });

    }

}
