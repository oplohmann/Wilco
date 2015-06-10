package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.WilcoCore;

/**
 * Created by plohmann on 19.02.2015.
 */
public class CloseQueueTask extends CoreTask {

    private WilcoCore core;
    private String queueId;

    public CloseQueueTask(WilcoCore core, String queueId) {
        this.core = core;
        this.queueId = queueId;
    }

    @Override
    public boolean run(Context context) {
        boolean queueSpineFound = core.removeQueue(queueId);
        assert queueSpineFound;
        clear();
        return true;
    }

    private void clear() {
        core = null;
    }

    @Override
    public int priority() {
        // Must be MEDIUM_PRIORITY same as with ScheduledTask and AsyncScheduledTask
        // for concatenation of channels to work.
        return MEDIUM_PRIORITY;
    }
}
