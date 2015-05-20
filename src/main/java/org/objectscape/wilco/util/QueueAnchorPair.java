package org.objectscape.wilco.util;

import org.objectscape.wilco.Queue;
import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.SchedulerControlled;

import java.util.List;

/**
 * Created by Nutzer on 18.05.2015.
 */
public class QueueAnchorPair {

    final private Queue queue;
    final private QueueAnchor anchor;

    public QueueAnchorPair(Queue queue, QueueAnchor anchor) {
        this.queue = queue;
        this.anchor = anchor;
    }

    public Queue getQueue() {
        return queue;
    }

    public QueueAnchor getAnchor() {
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

