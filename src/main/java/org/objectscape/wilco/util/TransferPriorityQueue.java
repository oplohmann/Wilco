package org.objectscape.wilco.util;

import org.objectscape.wilco.core.tasks.Task;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TransferQueue;

/**
 * Created by Nutzer on 22.02.2015.
 */
public class TransferPriorityQueue<T extends Task> {

    private TransferQueue<T> systemPriorityQueue = new LinkedTransferQueue<>();
    private TransferQueue<T> userPriorityQueue = new LinkedTransferQueue<>();

    private TransferQueue<T>[] priorityQueues = new TransferQueue[3];
    private Semaphore semaphore = new Semaphore(0);

    public TransferPriorityQueue()
    {
        priorityQueues[Task.SYSTEM_PRIORITY] = systemPriorityQueue;
        priorityQueues[Task.USER_PRIORITY] = userPriorityQueue;
    }

    public void add(T task) {
        priorityQueues[task.priority()].add(task);
        semaphore.release();
    }

    public T take() {
        semaphore.acquireUninterruptibly();
        return pollInternal();
    }

    private T pollInternal() {
        // Items of SYSTEM priority crowd out all other items. This is to make sure that internal admin tasks
        // are executed immediately.
        T task = systemPriorityQueue.poll();
        if(task != null) {
            return task;
        }
        // Items of USER priority are not picked up until all items of higher priority have been consumed.
        // Required for shutdown process to make sure the scheduler queue has run empty before shutting down.
        task = userPriorityQueue.poll();
        if(task != null) {
            return task;
        }
        return null;
    }

}
