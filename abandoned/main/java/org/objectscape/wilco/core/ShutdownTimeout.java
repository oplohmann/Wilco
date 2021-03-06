package org.objectscape.wilco.core;

import org.objectscape.wilco.AbstractQueue;
import org.objectscape.wilco.QueueSpine;
import org.objectscape.wilco.core.tasks.TryInitiateShutdownTask;
import org.objectscape.wilco.core.tasks.TryShutdownSchedulerTask;
import org.objectscape.wilco.core.tasks.util.ShutdownResponse;
import org.objectscape.wilco.core.tasks.util.ShutdownTaskInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Nutzer on 17.05.2015.
 */
public class ShutdownTimeout {

    final private WilcoCore core;
    final private ShutdownResponse shutdownResponse;
    final private int tryCount;
    final private CompletableFuture<ShutdownResponse> future;
    final private Consumer<ShutdownTimeout> callback;
    final protected List<QueueSpine> nonEmptyQueues;

    public ShutdownTimeout(WilcoCore core, CompletableFuture<ShutdownResponse> future, ShutdownResponse shutdownResponse, List<QueueSpine> nonEmptyQueues, Consumer<ShutdownTimeout> callback, int tryCount) {
        this.core = core;
        this.future = future;
        this.shutdownResponse = shutdownResponse;
        this.callback = callback;
        this.tryCount = tryCount;
        this.nonEmptyQueues = nonEmptyQueues;
    }

    public int tryCount() {
        return tryCount;
    }

    public void shutdownNow() {
        core.scheduleTask(new TryShutdownSchedulerTask(
                new ShutdownTaskInfo(core, future, 0, TimeUnit.MILLISECONDS, System.currentTimeMillis()),
                nonEmptyQueues,
                null,
                tryCount + 1,
                true));
    }

    public Map<AbstractQueue, List<Runnable>> getNotCompletedRunnablesByQueue() {
        return shutdownResponse.getNotCompletedRunnablesByQueue();
    }

    public Set<Runnable> getNotCompletedRunnables() {
        return shutdownResponse.getNotCompletedRunnables();
    }

    public Set<AbstractQueue> getNotCompletedQueues() {
        return shutdownResponse.getNotCompletedQueues();
    }

    public Set<String> getNotCompletedQueuesIds() {
        return shutdownResponse.getNotCompletedQueuesIds();
    }

    public boolean isShutdownCompleted() {
        return shutdownResponse.isShutdownCompleted();
    }

    public void retryShutdown(long duration, TimeUnit unit) {
        core.scheduleTask(new TryInitiateShutdownTask(
                toString(),
                new ShutdownTaskInfo(core, future, duration, unit, System.currentTimeMillis()),
                callback,
                nonEmptyQueues,
                tryCount + 1));
    }
}
