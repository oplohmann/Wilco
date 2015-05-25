package org.objectscape.wilco.util;

import org.objectscape.wilco.AbstractQueue;
import org.objectscape.wilco.core.AbstractQueueAnchor;
import org.objectscape.wilco.core.SchedulerControlled;

import java.util.List;

/**
 * Created by Nutzer on 18.05.2015.
 */
public class QueueAnchorPair {

    final private AbstractQueue queue;
    final private AbstractQueueAnchor anchor;

    public QueueAnchorPair(AbstractQueue queue, AbstractQueueAnchor anchor) {
        this.queue = queue;
        this.anchor = anchor;
    }

    public AbstractQueue getQueue() {
        return queue;
    }

    public AbstractQueueAnchor getAnchor() {
        return anchor;
    }

    public String getId() {
        return queue.getId();
    }

    public int size() {
        return queue.size();
    }

    @SchedulerControlled
    public List<Runnable> getUserRunnables() {
        return anchor.getUserRunnables();
    }
}

