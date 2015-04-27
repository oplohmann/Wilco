package org.objectscape.wilco.core;

import org.objectscape.wilco.util.LinkedQueue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by plohmann on 19.02.2015.
 */
public class QueueAnchor implements SchedulerControlled {

    final private String id;
    final private AtomicInteger size = new AtomicInteger(0);
    final private LinkedQueue<ScheduledRunnable> waitingTasks = new LinkedQueue<>();
    final private AtomicBoolean closed = new AtomicBoolean(false);
    private boolean suspended = false;

    public QueueAnchor(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int size() {
        return size.get();
    }

    public int decrementSize() {
        return size.decrementAndGet();
    }

    public boolean addTask(ScheduledRunnable runnable) {
        waitingTasks.addLast(runnable);
        size.incrementAndGet();
        if(suspended) {
            return false;
        }
        return waitingTasks.size() == 1;
    }

    public ScheduledRunnable removeCurrentTask() {
        size.decrementAndGet();
        return waitingTasks.removeFirst();
    }

    public ScheduledRunnable peekNextTask() {
        if(suspended) {
            return null;
        }
        return waitingTasks.peekFirst();
    }

    public boolean hasMoreTasks() {
        return waitingTasks.size() > 0;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public void close() {
        if(!closed.compareAndSet(false, true)) {
            // queue already closed
            throwQueueClosed();
        }
    }

    private void throwQueueClosed() {
        throw new QueueClosedException("queue " + id + " closed");
    }

    public boolean isClosed() {
        return closed.get();
    }

    public void checkClosed() {
        if(closed.get()) {
            throwQueueClosed();
        }
    }
}
