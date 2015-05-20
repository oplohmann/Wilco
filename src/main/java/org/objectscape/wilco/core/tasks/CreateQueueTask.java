package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.util.QueueAnchorPair;

/**
 * Created by plohmann on 19.02.2015.
 */
public class CreateQueueTask extends CoreTask {

    private WilcoCore core;
    private QueueAnchorPair queueAnchorPair;

    public CreateQueueTask(WilcoCore core, QueueAnchorPair queueAnchorPair) {
        this.core = core;
        this.queueAnchorPair = queueAnchorPair;
    }

    @Override
    public boolean run(Context context) {
        boolean success = core.addQueue(queueAnchorPair);
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
        queueAnchorPair = null;
    }

}
