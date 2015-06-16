package org.objectscape.wilco.core;

import org.objectscape.wilco.core.tasks.Task;
import org.objectscape.wilco.util.TransferPriorityQueue;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Context {

    // final private DeadLetterQueue deadLetterQueue;

    final private ThreadPoolExecutor executor;
    final private TransferPriorityQueue<Task> schedulerQueue;
    final private WilcoCore core;

    public Context(WilcoCore core, TransferPriorityQueue<Task> schedulerQueue, ThreadPoolExecutor executor) {
        this.core = core;
        this.schedulerQueue = schedulerQueue;
        this.executor = executor;
    }

    public WilcoCore getCore() {
        return core;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public TransferPriorityQueue<Task> getSchedulerQueue() {
        return schedulerQueue;
    }

}
