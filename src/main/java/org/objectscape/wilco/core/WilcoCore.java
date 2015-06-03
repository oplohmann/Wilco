package org.objectscape.wilco.core;

import org.objectscape.wilco.AsyncQueue;
import org.objectscape.wilco.Config;
import org.objectscape.wilco.QueueSpine;
import org.objectscape.wilco.core.dlq.DeadLetterEntry;
import org.objectscape.wilco.core.dlq.DeadLetterListener;
import org.objectscape.wilco.core.dlq.DeadLetterQueue;
import org.objectscape.wilco.core.tasks.CoreTask;
import org.objectscape.wilco.core.tasks.DetectIdleTask;
import org.objectscape.wilco.core.tasks.util.IdleInfo;
import org.objectscape.wilco.util.TransferPriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by plohmann on 19.02.2015.
 */
public class WilcoCore {

    final private static Logger LOG = LoggerFactory.getLogger(WilcoCore.class);

    private static AtomicInteger IdCount = new AtomicInteger(0);

    final private Config config;
    final private Scheduler scheduler;
    final private ThreadPoolExecutor executor;
    final private ScheduledThreadPoolExecutor scheduledExecutor;
    final private DeadLetterQueue deadLetterQueue = new DeadLetterQueue();
    final private Map<String, QueueSpine> queuesById = new TreeMap<>();
    final private QueueSpine asyncQueue;
    final private String id;

    private ScheduledFuture idleFuture;

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

        scheduledExecutor = new ScheduledThreadPoolExecutor(1);

        scheduler = new Scheduler(entryQueue, executor, deadLetterQueue);
        Thread thread = new Thread(scheduler);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private QueueSpine createAsyncQueue(String asyncQueueId) {
        AsyncQueueAnchor queueAnchor = new AsyncQueueAnchor(asyncQueueId);
        AsyncQueue queue = new AsyncQueue(queueAnchor, this);
        QueueSpine queueSpine = new QueueSpine(queue, queueAnchor);
        queuesById.put(asyncQueueId, queueSpine);
        return queueSpine;
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

    public boolean addQueue(QueueSpine queueSpine) {
        return queuesById.put(queueSpine.getId(), queueSpine) == null;
    }

    public boolean removeQueue(String queueId) {
        QueueSpine queueSpine = queuesById.remove(queueId);
        if(queueSpine != null) {
            // TODO - have to think about how to do this as this creates NullPointerExceptions
            // queueSpine.clear();
            return true;
        }
        return false;
    }

    @SchedulerControlled
    public List<QueueSpine> closeAllQueues() {
        LOG.info("Shutting down. Sending " + queuesById.size() + " queues the close signal");
        List<QueueSpine> queues = new ArrayList<>();
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

    public ScheduledFuture onIdleAfter(long timeoutPeriod, TimeUnit unit, Consumer<IdleInfo> consumer) {
        // synchronized is not a problem for lock contention here as an idle callback is only installed once
        synchronized (scheduledExecutor) {
            if(idleFuture != null) {
                if (idleFuture.isCancelled()) {
                    idleFuture = null;
                }
                else {
                    throw new IdleException("wilco on idle callback already installed");
                }
            }

            long timeoutPeriodInMillis = unit.toMillis(timeoutPeriod);

            idleFuture = scheduledExecutor.scheduleAtFixedRate(
                    () -> entryQueue.add(new DetectIdleTask(timeoutPeriodInMillis, queuesById, consumer)),
                    0, timeoutPeriod, unit);

            return idleFuture;
        }
    }

    public void cancelIdleTimer() {
        synchronized (scheduledExecutor) {
            if(idleFuture != null && !idleFuture.isCancelled()) {
                idleFuture.cancel(true);
            }
        }
    }
}
