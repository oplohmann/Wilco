package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.QueueSpine;
import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.WilcoCore;

/**
 * Created by plohmann on 19.02.2015.
 */
public class CreateQueueTask extends CoreTask {

    private WilcoCore core;
    private QueueSpine queueSpine;

    public CreateQueueTask(WilcoCore core, QueueSpine queueSpine) {
        this.core = core;
        this.queueSpine = queueSpine;
    }

    @Override
    public boolean run(Context context) {
        boolean success = core.addQueue(queueSpine);
        assert success;
        clear();
        return true;
    }

    @Override
    public int priority() {
        return MAX_PRIORITY;
    }

    private void clear() {
        core = null;
        queueSpine = null;
    }

}
