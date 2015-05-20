package org.objectscape.wilco.core;

import org.objectscape.wilco.util.LinkedQueue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by plohmann on 19.02.2015.
 */
public class QueueAnchor {

    final private String id;
    final private AtomicInteger userTasksCount = new AtomicInteger(0);
    final private LinkedQueue<ScheduledRunnable> waitingTasks = new LinkedQueue<>();
    private boolean suspended = false;

    public QueueAnchor(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int userTasksCount() {
        return userTasksCount.get();
    }

    public boolean addTask(ScheduledRunnable runnable) {
        waitingTasks.addLast(runnable);
        if(suspended) {
            return false;
        }
        return waitingTasks.size() == 1;
    }

    public boolean isEmpty() {
        return waitingTasks.size() == 0;
    }

    public ScheduledRunnable removeCurrentTask() {
        userTasksCount.decrementAndGet();
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

    public void incrementSize() {
        userTasksCount.incrementAndGet();
    }

    @SchedulerControlled
    public List<Runnable> getUserRunnables() {
        return waitingTasks.toList().stream().
            map(ScheduledRunnable::getRunnable).
            collect(Collectors.toList());
    }
}
