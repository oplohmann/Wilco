package org.objectscape.wilco.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nutzer on 22.05.2015.
 */
public class AsyncQueueAnchor extends AbstractQueueAnchor {

    private static int taskCount = 0;

    public static final String Id = "AsyncQueue";

    final private Map<Integer, ScheduledRunnable> waitingTasks = new HashMap<>();

    public AsyncQueueAnchor(String asyncQueueId) {
        super(asyncQueueId);
    }

    @SchedulerControlled
    public List<Runnable> getUserRunnables() {
        return toList(waitingTasks.values().stream().map(scheduledRunnable -> scheduledRunnable.getRunnable()));
    }

    @Override
    public int getWaitingTasksCount() {
        return waitingTasks.size();
    }

    @Override
    public boolean isSuspended() {
        return false; // async queue is always available
    }

    @Override
    public boolean isIdle() {
        return waitingTasks.isEmpty();
    }

    public int addTask(ScheduledRunnable scheduledRunnable) {
        taskCount++;
        waitingTasks.put(taskCount, scheduledRunnable);
        return taskCount;
    }

    public boolean removeTask(Integer taskId) {
        userTasksCount.decrementAndGet();
        return waitingTasks.remove(taskId) != null;
    }
}
