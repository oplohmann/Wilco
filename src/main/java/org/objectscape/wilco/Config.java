package org.objectscape.wilco;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Config {

    final private int corePoolSize;
    final private int maximumPoolSize;
    final private long keepAliveTime;
    final private TimeUnit unit;
    final private BlockingQueue<Runnable> queue;

    public Config() {
        corePoolSize = Runtime.getRuntime().availableProcessors() * 4;
        maximumPoolSize = corePoolSize * 2;
        keepAliveTime = 5000;
        unit = TimeUnit.MILLISECONDS;
        queue = createDefaultQueue();
    }

    public Config(int corePoolSize, int maximumPoolSize) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = 5000;
        this.unit = TimeUnit.MILLISECONDS;
        queue = createDefaultQueue();
    }

    public Config(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        queue = createDefaultQueue();
    }

    public Config(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> queue) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.queue = queue;
    }

    private BlockingQueue<Runnable> createDefaultQueue() {
        return new LinkedTransferQueue<>();
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public BlockingQueue<Runnable> getQueue() {
        return queue;
    }
}
