package org.objectscape.wilco.core;

import org.objectscape.wilco.Config;
import org.objectscape.wilco.core.dlq.DeadLetterEntry;
import org.objectscape.wilco.core.dlq.DeadLetterListener;
import org.objectscape.wilco.core.dlq.DeadLetterQueue;
import org.objectscape.wilco.core.tasks.CoreTask;
import org.objectscape.wilco.core.tasks.ScheduledTask;
import org.objectscape.wilco.util.TransferPriorityQueue;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 19.02.2015.
 */
public class WilcoCore {

    final private Config config;
    final private Scheduler scheduler;
    final private ThreadPoolExecutor executor;
    final private DeadLetterQueue deadLetterQueue = new DeadLetterQueue();

    private TransferPriorityQueue<CoreTask> entryQueue = new TransferPriorityQueue<>();

    public WilcoCore(Config config)
    {
        this.config = config;

        executor = new ThreadPoolExecutor(
            config.getCorePoolSize(),
            config.getMaximumPoolSize(),
            config.getKeepAliveTime(),
            config.getUnit(),
            config.getQueue());

        scheduler = new Scheduler(entryQueue, executor, deadLetterQueue);
        Thread thread = new Thread(scheduler);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    public void shutdown() {
        shutdown(10, TimeUnit.SECONDS);
    }

    public void shutdown(long duration, TimeUnit unit) {
        executor.shutdown();
        try {
            executor.awaitTermination(duration, unit);
        } catch (InterruptedException e) {
            deadLetterQueue.add(null, e);
        }
    }

    public void scheduleUserTask(ScheduledTask scheduledTask) {
        entryQueue.add(scheduledTask);
    }

    public void scheduleAdminTask(CoreTask coreTask) {
        entryQueue.add(coreTask);
    }

    public void addDLQListener(DeadLetterListener deadLetterListener) {
        deadLetterQueue.addListener(deadLetterListener);
    }

    public boolean removeDLQListener(DeadLetterListener deadLetterListener) {
        return deadLetterQueue.removeListener(deadLetterListener);
    }

    public List<DeadLetterEntry> getDLQEntries() {
        return deadLetterQueue.getDeadLetterEntries();
    }

    public void clearDLQ() {
        deadLetterQueue.clear();
    }
}
