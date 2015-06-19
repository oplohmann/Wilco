package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.QueueCore;
import org.objectscape.wilco.core.ScheduledRunnable;

/**
 * Created by plohmann on 19.06.2015.
 */
public abstract class QueueCoreTask extends UserTask {

    protected QueueCore queueCore;
    protected ScheduledRunnable scheduledRunnable;

    public QueueCoreTask(QueueCore queueCore, ScheduledRunnable scheduledRunnable) {
        this.queueCore = queueCore;
        this.scheduledRunnable = scheduledRunnable;
    }

    protected void executeNext(Context context) {
        ScheduledRunnable nextTask = queueCore.getNext();
        if(nextTask == null) {
            return;
        }

        CompletedTask completedTask = new CompletedTask(queueCore, nextTask);
        QueueCore tempQueueCore = queueCore;
        context.getExecutor().execute(() -> {
            try {
                nextTask.run();
            } catch (Exception e) {
                // TODO - NYI
            }
            tempQueueCore.execute(completedTask);
        });
    }

    @Override
    public void clear() {
        queueCore = null;
        scheduledRunnable = null;
    }
}
