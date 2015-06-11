package org.objectscape.wilco.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Context {

    // final private ThreadPoolExecutor executor;
    // final private TransferPriorityQueue<CoreTask> entryQueue;
    // final private DeadLetterQueue deadLetterQueue;
    private AtomicLong lastTimeActive;

    public long getLastTimeActive() {
        return lastTimeActive.get();
    }

    public void setLastTimeActive(long lastTimeActive) {
        this.lastTimeActive.getAndSet(lastTimeActive);
    }
}
