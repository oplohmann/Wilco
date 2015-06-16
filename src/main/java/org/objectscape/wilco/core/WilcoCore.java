package org.objectscape.wilco.core;

import org.objectscape.wilco.Config;
import org.objectscape.wilco.core.tasks.Task;
import org.objectscape.wilco.util.TransferPriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by plohmann on 11.06.2015.
 */
public class WilcoCore {

    final private static Logger LOG = LoggerFactory.getLogger(WilcoCore.class);

    private static AtomicInteger IdCount = new AtomicInteger(0);

    final private Config config;
    final private Scheduler scheduler;
    final private ThreadPoolExecutor executor;
    final private ScheduledThreadPoolExecutor scheduledExecutor;
    final private Map<String, QueueCore> queuesById = new TreeMap<>();
    final private String id;

    private TransferPriorityQueue<Task> schedulerQueue = new TransferPriorityQueue<>();

    public WilcoCore(Config config, String asyncQueueId)
    {
        this.config = config;

        id = String.valueOf(IdCount.getAndIncrement());

        executor = new ThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaximumPoolSize(),
                config.getKeepAliveTime(),
                config.getUnit(),
                config.getQueue());

        scheduledExecutor = new ScheduledThreadPoolExecutor(1);

        scheduler = new Scheduler(this, schedulerQueue, executor);
        Thread thread = new Thread(scheduler);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

}
