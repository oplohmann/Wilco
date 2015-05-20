package org.objectscape.wilco.core.tasks.util;

import org.objectscape.wilco.Queue;
import org.objectscape.wilco.util.CollectorsUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nutzer on 20.05.2015.
 */
public class ShutdownResponse implements CollectorsUtil {

    final private Map<Queue, List<Runnable>> nonEmptyQueues = new HashMap<>();

    public ShutdownResponse(Map<Queue, List<Runnable>> nonEmptyQueues) {
        this.nonEmptyQueues.putAll(nonEmptyQueues);
    }

    public boolean isShutdownCompleted() {
        return nonEmptyQueues.isEmpty();
    }

    public Map<Queue, List<Runnable>> getNotCompletedRunnablesByQueue() {
        return new HashMap<>(nonEmptyQueues);
    }

    public Set<Runnable> getNotCompletedRunnables() {
        return toSet(nonEmptyQueues.values().stream().flatMap(list -> list.stream()));
    }

    public Set<Queue> getNotCompletedQueues() {
        return nonEmptyQueues.keySet();
    }

    public Set<String> getNotCompletedQueuesIds() {
        return toSet(nonEmptyQueues.keySet().stream().map(queue -> queue.getId()));
    }
}
