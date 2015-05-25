package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.tasks.ResumeChannelTask;
import org.objectscape.wilco.core.tasks.ScheduledTask;
import org.objectscape.wilco.core.tasks.SuspendChannelTask;
import org.objectscape.wilco.util.ClosedOnceGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Queue extends AbstractQueue {

    private final static Logger LOG = LoggerFactory.getLogger(Queue.class);

    private QueueAnchor queueAnchor;

    public Queue(QueueAnchor queueAnchor, WilcoCore core) {
        super(core);
        this.queueAnchor = queueAnchor;
    }

    public String getId() {
        return queueAnchor.getId();
    }

    public int size() {
        return queueAnchor.userTasksCount();
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

    protected void clear() {
        // Free ref to core to make this object reachable by the GC.
        queueAnchor = null;
        super.clear();
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject)
            return true;
        if (otherObject == null || getClass() != otherObject.getClass())
            return false;

        Queue queue = (Queue) otherObject;

        if((queueAnchor == queue.queueAnchor) && (core == queue.core))
            return getId().equals(queue.getId());

        return false;
    }

    protected void incrementSize() {
        queueAnchor.incrementSize();
    }

    @Override
    public int hashCode() {
        int result = queueAnchor.hashCode();
        result = 31 * result + core.hashCode() + getId().hashCode();
        return result;
    }
}
