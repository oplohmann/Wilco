package org.objectscape.wilco.util;

import org.objectscape.wilco.core.tasks.CoreTask;

import java.util.Arrays;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TransferQueue;

/**
 * Created by Nutzer on 22.02.2015.
 */
public class TransferPriorityQueue<T extends CoreTask> {

    private TransferQueue<T> minPriorityQueue = new LinkedTransferQueue<>();
    private TransferQueue<T> mediumPriorityQueue = new LinkedTransferQueue<>();
    private TransferQueue<T> maxPriorityQueue = new LinkedTransferQueue<>();

    private TransferQueue<T>[] priorityQueues = new TransferQueue[3];
    private Semaphore semaphore = new Semaphore(0);

    public TransferPriorityQueue()
    {
        priorityQueues[CoreTask.MAX_PRIORITY] = maxPriorityQueue;
        priorityQueues[CoreTask.MEDIUM_PRIORITY] = mediumPriorityQueue;
        priorityQueues[CoreTask.MIN_PRIORITY] = minPriorityQueue;
    }

    public void add(T task) {
        priorityQueues[task.priority()].add(task);
        semaphore.release();
    }

    public T take() {
        return takeGeneral();
    }

    private T takeGeneral() {
        semaphore.acquireUninterruptibly();
        return pollInternal();
    }

    public T[] takeSlice(T[] slice)
    {
        // a little faster when the queue size is large
        if(slice.length == 0) {
            throw new IllegalArgumentException("slize must not be of length 0!");
        }

        for (int i = 0; i < slice.length; i++) {
            T task = pollInternal();
            if(task != null) {
                slice[i] = task;
            } else {
                break;
            }
        }

        int sliceSize = numElements(slice);

        if(sliceSize == 0) {
            semaphore.acquireUninterruptibly();
            slice[0] = pollInternal();
            return Arrays.copyOf(slice, 1);
        }

        semaphore.acquireUninterruptibly(sliceSize);

        return Arrays.copyOf(slice, sliceSize);
    }

    private T takeOptimized() {
        // WARNING: sometimes returns null. Not to be used as only experimental!
        // Idea: do not spend time on acquiring the lock in case the queue has entries
        T task = pollInternal();
        if(task != null) {
            return task;
        }

        // drainPermits() must be called, otherwise null might get returned which is an error for take()
        semaphore.drainPermits();
        semaphore.acquireUninterruptibly();
        return pollInternal();
    }

    private T pollInternal() {
        // Items of MAX priority crowd out all other items. This is to make sure that internal admin tasks
        // are executed immediately.
        T task = maxPriorityQueue.poll();
        if(task != null) {
            return task;
        }
        task = mediumPriorityQueue.poll();
        if(task != null) {
            return task;
        }
        // Items of MIN priority are not picked up until all items of higher priority have been consumed.
        // Required for shutdown process to make sure the global queue has run empty before shutting down.
        task = minPriorityQueue.poll();
        if(task != null) {
            return task;
        }
        return null;
    }

    private static <T> int numElements(T[] elements) {
        for (int i = 0; i <elements.length; i++) {
            if(elements[i] == null) {
                return i;
            }
        }
        return elements.length;
    }

}
