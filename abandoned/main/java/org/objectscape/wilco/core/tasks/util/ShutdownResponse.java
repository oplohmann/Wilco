package org.objectscape.wilco.core.tasks.util;

import org.objectscape.wilco.AbstractQueue;
import org.objectscape.wilco.util.CollectorsUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nutzer on 20.05.2015.
 */
public class ShutdownResponse implements CollectorsUtil {

    final private Map<AbstractQueue, List<Runnable>> nonEmptyQueues = new HashMap<>();

    public ShutdownResponse(Map<AbstractQueue, List<Runnable>> nonEmptyQueues) {
        this.nonEmptyQueues.putAll(nonEmptyQueues);
    }

    public boolean isShutdownCompleted() {
        return nonEmptyQueues.isEmpty();
    }

    public Map<AbstractQueue, List<Runnable>> getNotCompletedRunnablesByQueue() {
        return new HashMap<>(nonEmptyQueues);
    }

    public Set<Runnable> getNotCompletedRunnables() {
        return toSet(nonEmptyQueues.values().stream().flatMap(list -> list.stream()));
    }

    public Set<AbstractQueue> getNotCompletedQueues() {
        return nonEmptyQueues.keySet();
    }

    public Set<String> getNotCompletedQueuesIds() {
        return toSet(nonEmptyQueues.keySet().stream().map(queue -> queue.getId()));
    }
}
