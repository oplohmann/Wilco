package org.objectscape.wilco.core;

import org.objectscape.wilco.AsyncQueue;
import org.objectscape.wilco.Config;
import org.objectscape.wilco.core.dlq.DeadLetterEntry;
import org.objectscape.wilco.core.dlq.DeadLetterListener;
import org.objectscape.wilco.core.dlq.DeadLetterQueue;
import org.objectscape.wilco.core.tasks.CoreTask;
import org.objectscape.wilco.util.QueueAnchorPair;
import org.objectscape.wilco.util.TransferPriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by plohmann on 19.02.2015.
 */
public class WilcoCore {

    final private static Logger LOG = LoggerFactory.getLogger(WilcoCore.class);

    private static AtomicInteger IdCount = new AtomicInteger(0);

    final private Config config;
    final private Scheduler scheduler;
    final private ThreadPoolExecutor executor;
    final private DeadLetterQueue deadLetterQueue = new DeadLetterQueue();
    final private Map<String, QueueAnchorPair> queuesById = new TreeMap<>();
    final private QueueAnchorPair asyncQueue;
    final private String id;

    private TransferPriorityQueue<CoreTask> entryQueue = new TransferPriorityQueue<>();

    public WilcoCore(Config config, String asyncQueueId)
    {
        this.config = config;

        id = String.valueOf(IdCount.getAndIncrement());
        asyncQueue = createAsyncQueue(asyncQueueId);

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

    private QueueAnchorPair createAsyncQueue(String asyncQueueId) {
        AsyncQueueAnchor queueAnchor = new AsyncQueueAnchor(asyncQueueId);
        AsyncQueue queue = new AsyncQueue(queueAnchor, this);
        QueueAnchorPair queueAnchorPair = new QueueAnchorPair(queue, queueAnchor);
        queuesById.put(asyncQueueId, queueAnchorPair);
        return queueAnchorPair;
    }

    public void scheduleUserTask(CoreTask scheduledTask) {
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

    public boolean addQueue(QueueAnchorPair queueAnchorPair) {
        return queuesById.put(queueAnchorPair.getId(), queueAnchorPair) == null;
    }

    public boolean removeQueue(String queueId) {
        return queuesById.remove(queueId) != null;
    }

    @SchedulerControlled
    public List<QueueAnchorPair> closeAllQueues() {
        LOG.info("Shutting down. Sending " + queuesById.size() + " queues the close signal");
        List<QueueAnchorPair> queues = new ArrayList<>();
        queuesById.values().stream().forEach(queueWithAnchor -> {
            try {
                queues.add(queueWithAnchor);
                queueWithAnchor.getQueue().close();
            } catch (QueueClosedException e) {
                // does not matter in this situation
            }
        });
        return queues;
    }

    @SchedulerControlled
    public void commitShutdown() {
        queuesById.clear();
    }

    public boolean isSchedulerRunning() {
        return scheduler.isRunning();
    }

    public void scheduleAsyncUserTask(Runnable runnable) {
        asyncQueue.getQueue().execute(runnable);
    }

    public String getId() {
        return id;
    }
}
