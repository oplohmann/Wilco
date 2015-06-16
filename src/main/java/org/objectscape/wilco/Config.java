package org.objectscape.wilco;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Config {

    final public static int DefaultCorePoolSize = Runtime.getRuntime().availableProcessors() * 4;
    final public static int DefaultMaximumPoolSize = DefaultCorePoolSize * 2;
    final public static int DefaultKeepAliveTime = 5000;
    final public static int DefaultNumberOfSchedulers = 2;

    final private int corePoolSize;
    final private int maximumPoolSize;
    final private long keepAliveTime;
    final private TimeUnit unit;
    final private BlockingQueue<Runnable> queue;
    final private int numberOfSchedulers;

    public Config() {
        corePoolSize = DefaultCorePoolSize;
        maximumPoolSize = DefaultMaximumPoolSize;
        keepAliveTime = DefaultKeepAliveTime;
        unit = TimeUnit.MILLISECONDS;
        queue = createDefaultQueue();
        numberOfSchedulers = DefaultNumberOfSchedulers;
    }

    public Config(int corePoolSize, int maximumPoolSize, int numberOfSchedulers) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = DefaultKeepAliveTime;
        this.unit = TimeUnit.MILLISECONDS;
        this.queue = createDefaultQueue();
        this.numberOfSchedulers = numberOfSchedulers;
    }

    public Config(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.queue = createDefaultQueue();
        this.numberOfSchedulers = DefaultNumberOfSchedulers;
    }

    public Config(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> queue) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.queue = queue;
        this.numberOfSchedulers = DefaultNumberOfSchedulers;
    }

    public Config(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> queue, int numberOfSchedulers) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.queue = queue;
        this.numberOfSchedulers = numberOfSchedulers;
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

    public int getNumberOfSchedulers() {
        return numberOfSchedulers;
    }
}
