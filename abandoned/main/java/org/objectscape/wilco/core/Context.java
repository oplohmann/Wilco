package org.objectscape.wilco.core;

import org.objectscape.wilco.core.dlq.DeadLetterQueue;
import org.objectscape.wilco.core.tasks.CoreTask;
import org.objectscape.wilco.util.TransferPriorityQueue;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Context {

    final private ThreadPoolExecutor executor;
    final private TransferPriorityQueue<CoreTask> entryQueue;
    final private DeadLetterQueue deadLetterQueue;
    private AtomicLong lastTimeActive;

    public Context(ThreadPoolExecutor executor, TransferPriorityQueue<CoreTask> entryQueue, DeadLetterQueue deadLetterQueue, AtomicLong lastTimeActive) {
        this.executor = executor;
        this.entryQueue = entryQueue;
        this.deadLetterQueue = deadLetterQueue;
        this.lastTimeActive = lastTimeActive;
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

    public long getLastTimeActive() {
        return lastTimeActive.get();
    }

    public void setLastTimeActive(long lastTimeActive) {
        this.lastTimeActive.getAndSet(lastTimeActive);
    }
}
