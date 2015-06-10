package org.objectscape.wilco.core;

import org.objectscape.wilco.util.LinkedQueue;

import java.util.List;

/**
 * Created by plohmann on 19.02.2015.
 */
public class QueueAnchor extends AbstractQueueAnchor {

    final private LinkedQueue<ScheduledRunnable> waitingTasks = new LinkedQueue<>();
    private boolean suspended = false;

    public QueueAnchor(String id) {
        super(id);
    }

    public boolean addTask(ScheduledRunnable runnable) {
        waitingTasks.addLast(runnable);
        if(suspended) {
            return false;
        }
        return waitingTasks.size() == 1;
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

    public boolean isIdle() {
        return waitingTasks.isEmpty();
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public int getWaitingTasksCount() {
        return waitingTasks.size();
    }

    @SchedulerControlled
    public List<Runnable> getUserRunnables() {
        return toList(waitingTasks.toList().stream().map(scheduledRunnable -> scheduledRunnable.getRunnable()));
    }

}
