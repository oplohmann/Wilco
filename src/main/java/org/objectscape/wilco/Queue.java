package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.QueueClosedException;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.tasks.CloseQueueTask;
import org.objectscape.wilco.core.tasks.ResumeChannelTask;
import org.objectscape.wilco.core.tasks.ScheduledTask;
import org.objectscape.wilco.core.tasks.SuspendChannelTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Queue {

    private final static Logger LOG = LoggerFactory.getLogger(Queue.class);

    private final static boolean QUEUE_OPEN_MARK = false;
    private final static boolean QUEUE_CLOSED_MARK = true;

    private QueueAnchor queueAnchor;
    private WilcoCore core;

    final private AtomicMarkableReference<Thread> closeGuard = new AtomicMarkableReference(null, QUEUE_OPEN_MARK);

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
        try {
            lockForClosedOrOpen();
            core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable));
        }
        finally {
            unlock();
        }
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

        boolean wasAlreadyClosed = true;

        while(!closeGuard.isMarked()) {
            wasAlreadyClosed =! closeGuard.attemptMark(null, QUEUE_CLOSED_MARK);
        }

        if(wasAlreadyClosed) {
            throw new QueueClosedException("Queue " + getId() + " already closed");
        }

        try {
            lock(QUEUE_CLOSED_MARK);
            core.scheduleAdminTask(new CloseQueueTask(core, getId()));
        } finally {
            unlock();
        }
    }

    private void lockedForExecute(Runnable runnable) {
        try {
            lock(QUEUE_OPEN_MARK);
            runnable.run();
        }
        finally {
            unlock();
        }
    }

    private void lockedForExecuteUser(Runnable runnable) {
        try {
            lock(QUEUE_OPEN_MARK);
            queueAnchor.incrementSize();
            runnable.run();
        }
        finally {
            unlock();
        }
    }

    private void unlock() {
        // leave critical section
        if(closeGuard.isMarked()) {
            closeGuard.set(null, QUEUE_CLOSED_MARK);
        } else {
            closeGuard.set(null, QUEUE_OPEN_MARK);
        }

    }

    private void lock(boolean expectedAndNewMark) {
        Thread currentThread = Thread.currentThread();
        while(!closeGuard.compareAndSet(null, currentThread, expectedAndNewMark, expectedAndNewMark)) {
            if(closeGuard.isMarked()) {
                throw new QueueClosedException("Queue " + getId() + " closed");
            } else {
                LOG.debug("other thread won in Queue.close");
            }
        }
    }

    private boolean lockForClosedOrOpen() {
        Thread currentThread = Thread.currentThread();
        boolean expectedAndNewMark = closeGuard.isMarked() ? QUEUE_CLOSED_MARK : QUEUE_OPEN_MARK;
        while(!closeGuard.compareAndSet(null, currentThread, expectedAndNewMark, expectedAndNewMark)) {
            LOG.debug("other thread won in Queue.executeIgnoreClose");
        }
        return expectedAndNewMark;
    }

    public boolean isClosed() {
        return closeGuard.isMarked();
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
