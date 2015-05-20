package org.objectscape.wilco.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nutzer on 17.05.2015.
 */
public class ShutdownCallback {

    private Set<String> nonEmptyQueuesIds = new HashSet<>();

    public Set<String> getNonEmptyQueuesIds() {
        return nonEmptyQueuesIds;
    }

    public boolean isQueuesRunEmpty() {
        return nonEmptyQueuesIds.isEmpty();
    }

    public int tryCount() {
        return 0;
    }

    public void shutdownNow() {

    }

    public boolean isShutdownComplete() {
        return false;
    }

    public void retryShutdown(long duration, TimeUnit unit) {

    }
}
