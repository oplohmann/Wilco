package org.objectscape.wilco.core.dlq;

import org.objectscape.wilco.util.CallerMustSynchronize;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 25.03.2015.
 */
public class DeadLetterQueue {

    final private static int DefaultMaxAge = 1;
    final private static TimeUnit MaxAgeTimeUnit = TimeUnit.HOURS;

    final private LinkedList<DeadLetterEntry> entries = new LinkedList<>();
    final private List<DeadLetterListener> listeners = new ArrayList<>();
    final private Object lock = new Object();

    final private int maxSize;
    final private int maxAge;
    final private TimeUnit maxAgeTimeUnit;

    public DeadLetterQueue() {
        super();
        this.maxSize = Integer.MAX_VALUE;
        this.maxAge = DefaultMaxAge;
        this.maxAgeTimeUnit = MaxAgeTimeUnit;
    }

    public DeadLetterQueue(int maxSize) {
        super();
        this.maxSize = maxSize;
        this.maxAge = DefaultMaxAge;
        this.maxAgeTimeUnit = MaxAgeTimeUnit;
    }

    public DeadLetterQueue(int maxSize, int maxAge, TimeUnit maxAgeTimeUnit) {
        super();
        this.maxSize = maxSize;
        this.maxAge = maxAge;
        this.maxAgeTimeUnit = maxAgeTimeUnit;
    }

    public void add(String queueId, Throwable throwable) {
        synchronized (lock) {
            DeadLetterEntry deadLetterEntry = new DeadLetterEntry(queueId, throwable);
            entries.add(deadLetterEntry);
            if(entries.size() > maxSize) {
                entries.removeLast();
            }
            removeOutDatedEntries();
            applyListeners(deadLetterEntry);
        }
    }

    @CallerMustSynchronize
    private void removeOutDatedEntries() {
        long thresholdAge = System.currentTimeMillis() - maxAgeTimeUnit.toMillis(maxAge);
        LinkedList<DeadLetterEntry> entriesForIteration = new LinkedList<>(entries);
        for(DeadLetterEntry entry : entriesForIteration) {
            if(entry.getCreationTime() < thresholdAge) {
                entries.remove(entry);
            }
        }
    }

    public void clear() {
        synchronized (lock) {
            entries.clear();
        }
    }

    public void addListener(DeadLetterListener listener) {
        synchronized (lock) {
            listeners.add(listener);
        }
    }

    @CallerMustSynchronize
    private void applyListeners(DeadLetterEntry entry) {
        for(DeadLetterListener listener : listeners) {
            if(listener.matches(entry)) {
                listener.getCallback().accept(entry);
            }
        }
    }

    public boolean removeListener(DeadLetterListener deadLetterListener) {
        synchronized (lock) {
            return listeners.remove(deadLetterListener);
        }
    }

    public List<DeadLetterEntry> getDeadLetterEntries() {
        synchronized (lock) {
            return new ArrayList<>(entries);
        }
    }
}
