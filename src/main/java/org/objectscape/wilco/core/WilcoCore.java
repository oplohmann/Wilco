package org.objectscape.wilco.core;

import org.objectscape.wilco.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    final private Scheduler[] schedulers;
    final private ThreadPoolExecutor executor;
    final private ScheduledThreadPoolExecutor scheduledExecutor;
    final private String id;

    public WilcoCore(Config config)
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

        schedulers = new Scheduler[config.getNumberOfSchedulers()];

        for (int i = 0; i < config.getNumberOfSchedulers(); i++) {
            schedulers[i] = new Scheduler(this, executor).start();
        }
    }

    public Scheduler getLeastLoadedScheduler() {
        // Very simple strategy for the time being.
        Scheduler leastLoadedScheduler = schedulers[0];
        for(Scheduler scheduler : schedulers) {
            if(scheduler.getQueueCount() < leastLoadedScheduler.getQueueCount()) {
                leastLoadedScheduler = scheduler;
            }
        }
        return leastLoadedScheduler;
    }
}
