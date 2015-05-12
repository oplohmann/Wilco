package org.objectscape.wilco.core;

import org.objectscape.wilco.Config;
import org.objectscape.wilco.Queue;
import org.objectscape.wilco.core.dlq.DeadLetterEntry;
import org.objectscape.wilco.core.dlq.DeadLetterListener;
import org.objectscape.wilco.core.dlq.DeadLetterQueue;
import org.objectscape.wilco.core.tasks.CoreTask;
import org.objectscape.wilco.core.tasks.ScheduledTask;
import org.objectscape.wilco.util.TransferPriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 19.02.2015.
 */
public class WilcoCore {

    final private static Logger LOG = LoggerFactory.getLogger(WilcoCore.class);

    final private Config config;
    final private Scheduler scheduler;
    final private ThreadPoolExecutor executor;
    final private DeadLetterQueue deadLetterQueue = new DeadLetterQueue();
    final private Map<String, Queue> queuesById = new HashMap<>();

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

    public boolean addQueue(Queue queue) {
        return queuesById.put(queue.getId(), queue) == null;
    }

    public boolean removeQueue(String queueId) {
        return queuesById.remove(queueId) != null;
    }

    public boolean prepareShutdown(long duration, TimeUnit unit) {
        long start = System.currentTimeMillis();
        long durationInMillis = unit.toMillis(duration);
        long end = start + durationInMillis;
        LOG.info("Shutting down. Sending " + queuesById.size() + " queues the close signal");
        List<Queue> nonEmptyQueues = new ArrayList<>();
        for(Map.Entry<String, Queue> entry : queuesById.entrySet()) {
            Queue queue = entry.getValue();
            assert entry.getKey().equals(queue.getId());
            int size = queue.size();
            try {
                queue.close();
            } catch (QueueClosedException e) {
                size = 0;
            }
            LOG.info("closed queue " + queue.getId() + " containing " + size + " tasks");
            if(size > 0) {
                nonEmptyQueues.add(queue);
            }
        }

        long buffer = Math.round(durationInMillis * 0.1);

        while (true) {
            List<Queue> remainingNonEmptyQueues = new ArrayList<>(nonEmptyQueues);
            for(Queue queue : remainingNonEmptyQueues) {
                assert queue.isClosed();
                if(queue.isEmpty()) {
                    nonEmptyQueues.remove(queue);
                }
            }
            if(nonEmptyQueues.isEmpty()) {
                return true;
            }
            if(System.currentTimeMillis() + buffer >= end) {
                return false;
            }
        }
    }

    public int commitShutdown() {
        int numberOfRunningTasks = queuesById.values().stream().mapToInt(Queue::size).sum();
        queuesById.clear();
        return numberOfRunningTasks;
    }

}
