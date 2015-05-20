package org.objectscape.wilco.core;

import org.objectscape.wilco.Queue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Nutzer on 20.05.2015.
 */
public class ShutdownResponse {

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
        return nonEmptyQueues.values().stream().flatMap(list -> list.stream()).collect(Collectors.toSet());
    }

    public Set<Queue> getNotCompletedQueues() {
        return nonEmptyQueues.keySet();
    }

    public Set<String> getNotCompletedQueuesIds() {
        return nonEmptyQueues.keySet().stream().map(queue -> queue.getId()).collect(Collectors.toSet());
    }
}
