package org.objectscape.wilco.util;

import org.objectscape.wilco.Queue;
import org.objectscape.wilco.core.QueueAnchor;

/**
 * Created by Nutzer on 18.05.2015.
 */
public class QueueWithAnchor {

    final private Queue queue;
    final private QueueAnchor anchor;

    public QueueWithAnchor(Queue queue, QueueAnchor anchor) {
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
}

