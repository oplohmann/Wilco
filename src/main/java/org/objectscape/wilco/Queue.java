package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.QueueClosedException;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.tasks.CloseQueueTask;
import org.objectscape.wilco.core.tasks.ResumeChannelTask;
import org.objectscape.wilco.core.tasks.ScheduledTask;
import org.objectscape.wilco.core.tasks.SuspendChannelTask;
import org.objectscape.wilco.util.ClosedOnceGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Queue {

    private final static Logger LOG = LoggerFactory.getLogger(Queue.class);

    private QueueAnchor queueAnchor;
    private WilcoCore core;

    private ClosedOnceGuard closedGuard = new ClosedOnceGuard();

    public Queue(QueueAnchor queueAnchor, WilcoCore core) {
        this.queueAnchor = queueAnchor;
        this.core = core;
    }

    public String getId() {
        return queueAnchor.getId();
    }

    public int size() {
        return queueAnchor.userTasksCount();
    }

    public boolean isEmpty() {
        return queueAnchor.userTasksCount() == 0;
    }

    public void execute(Runnable runnable) {
        lockedForExecuteUser(() -> core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable)));
    }

    public void execute(Runnable runnable, Runnable whenDoneRunnable) {
        lockedForExecuteUser(() -> core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable, whenDoneRunnable)));
    }

    protected void executeIgnoreClose(Runnable runnable) {
        // Does not check whether closed. Therefore also no QueueClosedException is thrown
        ClosedOnceGuard.Mark expectedAndNewMark = closedGuard.isClosed() ? ClosedOnceGuard.Mark.CLOSED : ClosedOnceGuard.Mark.CLOSED;
        closedGuard.run(expectedAndNewMark, () -> core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable)));
    }

    public void suspend() {
        lockedForExecute(()-> core.scheduleAdminTask(new SuspendChannelTask(queueAnchor)));
    }

    public void suspend(Runnable whenDoneRunnable) {
        lockedForExecute(()-> core.scheduleAdminTask(new SuspendChannelTask(queueAnchor, whenDoneRunnable)));
    }

    public void resume() {
        lockedForExecute(()-> core.scheduleAdminTask(new ResumeChannelTask(queueAnchor)));
    }

    public void resume(Runnable whenDoneRunnable) {
        lockedForExecute(()-> core.scheduleAdminTask(new ResumeChannelTask(queueAnchor, whenDoneRunnable)));
    }

    public void close() {
        boolean wasOpen = closedGuard.closeAndRun(() -> core.scheduleAdminTask(new CloseQueueTask(core, getId())));
        if(!wasOpen) {
            throw new QueueClosedException("Queue " + getId() + " already closed");
        }
    }

    private void lockedForExecute(Runnable runnable) {
        boolean wasOpen = closedGuard.runIfOpen(runnable);
        if(!wasOpen) {
            throw new QueueClosedException("Queue " + getId() + " closed");
        }
    }

    private void lockedForExecuteUser(Runnable runnable) {
        Runnable userRunnable = ()-> {
            queueAnchor.incrementSize();
            runnable.run();
        };
        if(!closedGuard.runIfOpen(userRunnable)) {
            throw new QueueClosedException("Queue " + getId() + " closed");
        }
    }

    public boolean isClosed() {
        return closedGuard.isClosed();
    }

    protected void clear() {
        // Free ref to core to make this object reachable by the GC.
        core = null;
        queueAnchor = null;
    }

    protected void finalize() throws Throwable {
        try {
            clear();
        } finally {
            super.finalize();
        }
    }
}
