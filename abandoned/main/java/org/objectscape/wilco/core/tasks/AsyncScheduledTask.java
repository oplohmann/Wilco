package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.AsyncQueueAnchor;
import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.ScheduledRunnable;

/**
 * Created by Nutzer on 25.05.2015.
 */
public class AsyncScheduledTask extends ScheduledRunnableTask {

    private AsyncQueueAnchor queueAnchor;

    public AsyncScheduledTask(AsyncQueueAnchor queueAnchor, ScheduledRunnable scheduledRunnable) {
        super(scheduledRunnable);
        this.queueAnchor = queueAnchor;
    }

    @Override
    public boolean run(Context context) {
        int taskId = queueAnchor.addTask(scheduledRunnable);
        executeNext(context, scheduledRunnable, taskId);
        setLastTimeActive(context);
        return true;
    }

    @Override
    protected CoreTask createCompletedTask(ScheduledRunnable nextRunnable, Integer taskId) {
        return new CompletedAsyncTask(queueAnchor, taskId);
    }

    @Override
    protected CoreTask createCompletedTask(ScheduledRunnable nextRunnable) {
        throw new RuntimeException("illegal message for this kind of object");
    }

    @Override
    protected void clear() {
        queueAnchor = null;
        super.clear();
    }
}
