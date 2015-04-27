package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.ScheduledRunnable;

/**
 * Created by plohmann on 23.03.2015.
 */
public abstract class QueueAnchorTask extends CoreTask {

    protected QueueAnchor queueAnchor;
    protected ScheduledRunnable scheduledRunnable;

    public QueueAnchorTask(QueueAnchor queueAnchor) {
        this.queueAnchor = queueAnchor;
        this.scheduledRunnable = null;
    }

    public QueueAnchorTask(QueueAnchor queueAnchor, Runnable userRunnable) {
        this.queueAnchor = queueAnchor;
        this.scheduledRunnable = new ScheduledRunnable(userRunnable);
    }

    public QueueAnchorTask(QueueAnchor queueAnchor, Runnable userRunnable, Runnable whenDoneRunnable) {
        this.queueAnchor = queueAnchor;
        this.scheduledRunnable = new ScheduledRunnable(userRunnable, whenDoneRunnable);
    }

    public QueueAnchorTask(QueueAnchor queueAnchor, ScheduledRunnable nextRunnable) {
        this.queueAnchor = queueAnchor;
        this.scheduledRunnable = nextRunnable;
    }

    protected void executeNext(final Context context, ScheduledRunnable nextRunnable) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    nextRunnable.run();
                } catch (Throwable throwable) {
                    context.addToDeadLetterQueue(queueAnchor.getId(), throwable);
                }
                context.getEntryQueue().add(new CompletedTask(queueAnchor, nextRunnable));
                clear();
            }
        };
        context.getExecutor().execute(runnable);
    }

    protected void runWhenDone(Context context) {
        if(scheduledRunnable != null) {
            try {
                scheduledRunnable.runWhenDone();
            } catch (Exception e) {
                context.addToDeadLetterQueue(queueId(), e);
            }
        }
    }

    protected void clear() {
        // make sure this instance does not cling to long-lived objects
        queueAnchor = null;
        scheduledRunnable = null;
    }

    @Override
    public String queueId() {
        return queueAnchor.getId();
    }
}
