package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.tasks.util.ShutdownTaskInfo;
import org.objectscape.wilco.util.CollectorsUtil;
import org.objectscape.wilco.util.QueueAnchorPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nutzer on 18.05.2015.
 */
public class RunEmptyShutdownTask extends ShutdownTask implements CollectorsUtil {

    final private List<QueueAnchorPair> allQueues;

    public RunEmptyShutdownTask(ShutdownTaskInfo shutdownTaskInfo, List<QueueAnchorPair> allQueues) {
        super(shutdownTaskInfo);
        this.allQueues = allQueues;
    }

    @Override
    public boolean run(Context context) {
        List<QueueAnchorPair> nonEmptyQueues = toList(allQueues.stream().filter(queueAnchor -> !queueAnchor.getQueue().isEmpty()));
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
        getCore().scheduleAdminTask(getShutdownSchedulerTask(nonEmptyQueues));
    }

    protected ShutdownSchedulerTask getShutdownSchedulerTask(List<QueueAnchorPair> nonEmptyQueues) {
        return new ShutdownSchedulerTask(shutdownTaskInfo, nonEmptyQueues);
    }

    private void waitTilRunEmptyThenShutdown(Context context, List<QueueAnchorPair> nonEmptyQueues) {
        // This tasks currently blocks the Scheduler and therefore pending tasks cannot be dispatched.
        // For the thread pool to be able to run empty, this task needs to release the Scheduler.
        // For that purpose waiting for all queues to run empty before shutting down the Scheduler and the thread
        // pool is done in a separate thread by invoking the thread pool executor as in the code below.
        context.getExecutor().execute(()-> runEmpty(nonEmptyQueues));

    }

    protected void runEmpty(List<QueueAnchorPair> nonEmptyQueues) {
        long durationInMillis = getDurationInMillis();
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
                    long remainingShutdownTime = durationInMillis - (System.currentTimeMillis() - getStart());
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
    }

}
