package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.AbstractQueue;
import org.objectscape.wilco.QueueSpine;
import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.tasks.util.ShutdownResponse;
import org.objectscape.wilco.core.tasks.util.ShutdownTaskInfo;
import org.objectscape.wilco.util.CollectorsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nutzer on 15.05.2015.
 */
public class ShutdownSchedulerTask extends ShutdownTask implements CollectorsUtil {

    final protected List<QueueSpine> nonEmptyQueues;

    public ShutdownSchedulerTask(ShutdownTaskInfo shutdownTaskInfo, List<QueueSpine> nonEmptyQueues) {
        super(shutdownTaskInfo);
        this.nonEmptyQueues = nonEmptyQueues;
    }

    @Override
    public boolean run(Context context) {
        ShutdownResponse numberOfRunningTasks = shutdownThreadPoolAndScheduler(context);
        getDoneSignal().complete(numberOfRunningTasks);
        return false; // returning false will make the Scheduler exit its main loop
    }

    @Override
    public int priority() {
        return MAX_PRIORITY;
    }

    protected ShutdownResponse shutdownThreadPoolAndScheduler(Context context) {
        long remainingShutdownTime = getDurationInMillis() - (System.currentTimeMillis() - getStart());
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
        getCore().commitShutdown();
        return createShutdownResponse();
    }

    protected ShutdownResponse createShutdownResponse() {
        Map<AbstractQueue, List<Runnable>> notCompletedRunnablesByQueue = new HashMap<>();
        for(QueueSpine queueSpine : nonEmptyQueues) {
            List<Runnable> notCompletedRunnables = notCompletedRunnablesByQueue.get(queueSpine.getQueue());
            if(notCompletedRunnables == null) {
                notCompletedRunnables = new ArrayList<>();
                notCompletedRunnablesByQueue.put(queueSpine.getQueue(), notCompletedRunnables);
            }
            notCompletedRunnables.addAll(queueSpine.getUserRunnables());
        }
        return new ShutdownResponse(notCompletedRunnablesByQueue);
    }

}
