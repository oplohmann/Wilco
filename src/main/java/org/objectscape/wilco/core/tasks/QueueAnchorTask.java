package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.ScheduledRunnable;

import java.util.Optional;

/**
 * Created by plohmann on 23.03.2015.
 */
public abstract class QueueAnchorTask extends ScheduledRunnableTask {

    protected QueueAnchor queueAnchor;

    public QueueAnchorTask(QueueAnchor queueAnchor) {
        this.queueAnchor = queueAnchor;
        this.scheduledRunnable = null;
    }

    public QueueAnchorTask(QueueAnchor queueAnchor, Runnable userRunnable) {
        super(new ScheduledRunnable(userRunnable));
        this.queueAnchor = queueAnchor;
    }

    public QueueAnchorTask(QueueAnchor queueAnchor, Runnable userRunnable, Runnable whenDoneRunnable) {
        super(new ScheduledRunnable(userRunnable, whenDoneRunnable));
        this.queueAnchor = queueAnchor;
    }

    public QueueAnchorTask(QueueAnchor queueAnchor, ScheduledRunnable nextRunnable) {
        super(nextRunnable);
        this.queueAnchor = queueAnchor;
    }

    protected CompletedTask createCompletedTask(ScheduledRunnable nextRunnable) {
        return new CompletedTask(queueAnchor, nextRunnable);
    }

    @Override
    protected CoreTask createCompletedTask(ScheduledRunnable nextRunnable, Integer taskId) {
        return null;
    }

    protected void clear() {
        // make sure this instance does not cling to long-lived objects
        super.clear();
        queueAnchor = null;
    }

    @Override
    public Optional<String> queueId() {
        return Optional.of(queueAnchor.getId());
    }
}
