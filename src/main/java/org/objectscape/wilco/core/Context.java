package org.objectscape.wilco.core;

import org.objectscape.wilco.core.dlc.DeadLetterQueue;
import org.objectscape.wilco.core.tasks.CoreTask;
import org.objectscape.wilco.util.TransferPriorityQueue;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TransferQueue;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Context {

    final private ThreadPoolExecutor executor;
    final private TransferPriorityQueue<CoreTask> entryQueue;
    final private DeadLetterQueue deadLetterQueue;

    public Context(ThreadPoolExecutor executor, TransferPriorityQueue<CoreTask> entryQueue, DeadLetterQueue deadLetterQueue) {
        this.executor = executor;
        this.entryQueue = entryQueue;
        this.deadLetterQueue = deadLetterQueue;
    }

    public void addToDeadLetterQueue(String queueId, Throwable throwable) {
        deadLetterQueue.add(queueId, throwable);
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public TransferPriorityQueue<CoreTask> getEntryQueue() {
        return entryQueue;
    }

    public DeadLetterQueue getDeadLetterQueue() {
        return deadLetterQueue;
    }
}
