package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.QueueClosedException;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.tasks.CloseQueueTask;
import org.objectscape.wilco.core.tasks.ResumeChannelTask;
import org.objectscape.wilco.core.tasks.ScheduledTask;
import org.objectscape.wilco.core.tasks.SuspendChannelTask;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Queue {

    private final static boolean QUEUE_OPEN_MARK = false;
    private final static boolean QUEUE_CLOSED_MARK = true;

    final private QueueAnchor queueAnchor;
    final private WilcoCore core;

    final private AtomicMarkableReference<Thread> guard = new AtomicMarkableReference(null, QUEUE_OPEN_MARK);

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

    public void execute(Runnable runnable) {
        lockedForExecuteUser(() -> core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable)));
    }

    public void execute(Runnable runnable, Runnable whenDoneRunnable) {
        lockedForExecuteUser(() -> core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable, whenDoneRunnable)));
    }

    protected void executeIgnoreClose(Runnable runnable) {
        // does not check whether closed.
        boolean mark = lockForClosedOrOpen();
        try {
            core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable));
        }
        finally {
            unlock(mark);
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

        while(!guard.isMarked()) {
            wasAlreadyClosed =! guard.attemptMark(null, QUEUE_CLOSED_MARK);
        }

        if(wasAlreadyClosed) {
            throw new QueueClosedException("Queue " + getId() + " already closed");
        }

        try {
            lockForClose();
            core.scheduleAdminTask(new CloseQueueTask());
        } finally {
            unlock(QUEUE_CLOSED_MARK);
        }
    }

    private void lockedForExecute(Runnable runnable) {
        boolean mark = lockForExecute();
        try {
            runnable.run();
        }
        finally {
            unlock(mark);
        }
    }

    private void lockedForExecuteUser(Runnable runnable) {
        boolean mark = lockForExecute();
        try {
            queueAnchor.incrementSize();
            runnable.run();
        }
        finally {
            unlock(mark);
        }
    }

    private void unlock(boolean mark) {
        guard.set(null, mark); // leave critical section
    }

    private void lockForClose() {
        Thread currentThread = Thread.currentThread();
        while(!guard.compareAndSet(null, currentThread, QUEUE_CLOSED_MARK, QUEUE_CLOSED_MARK)) {
            if(guard.isMarked()) {
                throw new QueueClosedException("Queue " + getId() + " already closed");
            } else {
                otherThreadWon();
            }
        }
    }

    private void otherThreadWon() {
        // other thread won, keep spinning to obtain the lock
        // for testing/debugging
        System.out.println("other thread won");
    }

    private boolean lockForClosedOrOpen() {
        Thread currentThread = Thread.currentThread();
        boolean mark = QUEUE_OPEN_MARK;
        if(guard.isMarked()) {
            mark = QUEUE_CLOSED_MARK;
        }
        while(!guard.compareAndSet(null, currentThread, mark, mark)) {
            otherThreadWon();
        }
        return mark;
    }

    private boolean lockForExecute() {
        Thread currentThread = Thread.currentThread();
        while(!guard.compareAndSet(null, currentThread, QUEUE_OPEN_MARK, QUEUE_OPEN_MARK)) {
            if(guard.isMarked()) {
                throw new QueueClosedException("Queue " + getId() + " closed");
            } else {
                otherThreadWon();
            }
        }
        return QUEUE_OPEN_MARK;
    }

    public boolean isClosed() {
        return guard.isMarked();
    }

}
