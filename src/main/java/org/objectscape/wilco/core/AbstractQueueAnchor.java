package org.objectscape.wilco.core;

import org.objectscape.wilco.util.CollectorsUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nutzer on 22.05.2015.
 */
public abstract class AbstractQueueAnchor implements CollectorsUtil {

    final protected String id;
    final protected AtomicInteger userTasksCount = new AtomicInteger(0);

    public AbstractQueueAnchor(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int userTasksCount() {
        return userTasksCount.get();
    }

    public void incrementSize() {
        userTasksCount.incrementAndGet();
    }

    @SchedulerControlled
    public abstract List<Runnable> getUserRunnables();

    public abstract int getWaitingTasksCount();

    public abstract boolean isSuspended();

    public abstract boolean isIdle();
}
