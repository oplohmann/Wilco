package org.objectscape.wilco.core;

import org.objectscape.wilco.core.tasks.Task;
import org.objectscape.wilco.util.LinkedQueue;
import org.objectscape.wilco.util.TransferPriorityQueue;

/**
 * Created by plohmann on 11.06.2015.
 */
public class QueueCore {

    TransferPriorityQueue<Task> schedulerQueue;
    final private LinkedQueue<ScheduledRunnable> waitingTasks = new LinkedQueue<>();
    private boolean suspended = false;
    final private String id;

    public QueueCore(TransferPriorityQueue<Task> schedulerQueue, String queueId, String schedulerId) {
        super();
        this.schedulerQueue = schedulerQueue;
        this.id = schedulerId + ", queue=" + queueId;
    }

    public String getId() {
        return id;
    }

    public boolean addTask(ScheduledRunnable scheduledRunnable) {
        waitingTasks.addLast(scheduledRunnable);
        if(suspended) {
            return false;
        }
        return waitingTasks.size() == 1;
    }

    public ScheduledRunnable getNext() {
        return waitingTasks.peekFirst();
    }

    public void execute(Task task) {
        schedulerQueue.add(task);
    }

    public ScheduledRunnable removeCurentTask() {
        return waitingTasks.pollFirst();
    }

}
