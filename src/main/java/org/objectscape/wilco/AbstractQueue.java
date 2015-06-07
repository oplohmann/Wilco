package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueClosedException;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.tasks.CloseQueueTask;
import org.objectscape.wilco.util.ClosedOnceGuard;

/**
 * Created by Nutzer on 25.05.2015.
 */
public abstract class AbstractQueue {

    protected WilcoCore core;

    protected final ClosedOnceGuard closedGuard = new ClosedOnceGuard();

    public AbstractQueue(WilcoCore core) {
        this.core = core;
    }

    public abstract String getId();
    public abstract int size();

    public abstract void execute(Runnable runnable);
    public abstract void execute(Runnable runnable, Runnable whenDoneRunnable);

    public boolean isClosed() {
        return closedGuard.isClosed();
    }

    public void close() {
        scheduleClose(new CloseQueueTask(core, getId()));
    }

    protected void scheduleClose(CloseQueueTask task) {
        boolean wasOpen = closedGuard.closeAndRun(() -> core.scheduleTask(task));
        if(!wasOpen) {
            throw new QueueClosedException("Queue " + getId() + " already closed");
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    protected void lockedForExecute(Runnable runnable) {
        boolean wasOpen = closedGuard.runIfOpen(runnable);
        if(!wasOpen) {
            throw new QueueClosedException("Queue " + getId() + " closed");
        }
    }

    protected void lockedForExecuteUser(Runnable runnable) {
        Runnable userRunnable = ()-> {
            incrementSize();
            runnable.run();
        };
        if(!closedGuard.runIfOpen(userRunnable)) {
            throw new QueueClosedException("Queue " + getId() + " closed");
        }
    }

    protected abstract void incrementSize();

    protected void clear() {
        core = null;
    }

    protected void finalize() throws Throwable {
        try {
            clear();
        } finally {
            super.finalize();
        }
    }
}
