package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueCore;
import org.objectscape.wilco.core.ScheduledRunnable;
import org.objectscape.wilco.core.tasks.ScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by plohmann on 11.06.2015.
 */
public class Queue {

    private final static Logger LOG = LoggerFactory.getLogger(Queue.class);

    private QueueCore core;

    public Queue(QueueCore core) {
        super();
        this.core = core;
    }

    public void execute(Runnable runnable) {
        core.execute(new ScheduledTask(core, new ScheduledRunnable(runnable)));
    }

    public String toString() {
        return "Queue{" + getId() + "}";
    }

    public String getId() {
        return core.getId();
    }
}
