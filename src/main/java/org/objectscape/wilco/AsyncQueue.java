package org.objectscape.wilco;

import org.objectscape.wilco.core.AsyncQueueAnchor;
import org.objectscape.wilco.core.ScheduledRunnable;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.tasks.AsyncScheduledTask;

/**
 * Created by Nutzer on 25.05.2015.
 */
public class AsyncQueue extends AbstractQueue {

    private AsyncQueueAnchor queueAnchor;

    public AsyncQueue(AsyncQueueAnchor queueAnchor, WilcoCore core) {
        super(core);
        this.queueAnchor = queueAnchor;
    }

    @Override
    public String getId() {
        return queueAnchor.getId();
    }

    @Override
    public int size() {
        return queueAnchor.userTasksCount();
    }

    public void execute(Runnable runnable) {
        lockedForExecuteUser(() -> core.scheduleUserTask(new AsyncScheduledTask(queueAnchor, new ScheduledRunnable(runnable))));
    }

    public void execute(Runnable runnable, Runnable whenDoneRunnable) {
        lockedForExecuteUser(() -> core.scheduleUserTask(null));
    }

    @Override
    protected void incrementSize() {
        queueAnchor.incrementSize();
    }
}
