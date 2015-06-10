package org.objectscape.wilco.core.tasks.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by plohmann on 29.05.2015.
 */
public class IdleInfo {

    final private long noActivitySince;
    final private Set<String> suspendedQueuesIds;
    final private Set<String> idleQueuesIds;
    final private long totalQueuesCount;

    public IdleInfo(long noActivitySince, Set<String> suspendedQueuesIds, Set<String> idleQueuesIds, long totalQueuesCount) {
        this.noActivitySince = noActivitySince;
        this.suspendedQueuesIds = suspendedQueuesIds;
        this.idleQueuesIds = idleQueuesIds;
        this.totalQueuesCount = totalQueuesCount;
    }

    public long getNoActivitySince() {
        return noActivitySince;
    }

    public Set<String> getSuspendedQueuesIds() {
        return new HashSet<>(suspendedQueuesIds);
    }

    public Set<String> getIdleQueuesIds() {
        return new HashSet<>(idleQueuesIds);
    }

    public long getTotalQueuesCount() {
        return totalQueuesCount;
    }
}
