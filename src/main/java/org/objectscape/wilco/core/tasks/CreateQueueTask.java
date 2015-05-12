package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.Queue;
import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.WilcoCore;

/**
 * Created by plohmann on 19.02.2015.
 */
public class CreateQueueTask extends CoreTask {

    private WilcoCore core;
    private Queue queue;

    public CreateQueueTask(WilcoCore core, Queue queue) {
        this.core = core;
        this.queue = queue;
    }

    @Override
    public boolean run(Context context) {
        boolean success = core.addQueue(queue);
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
        queue = null;
    }

}
