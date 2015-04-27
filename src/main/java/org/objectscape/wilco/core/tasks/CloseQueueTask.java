package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;

/**
 * Created by plohmann on 19.02.2015.
 */
public class CloseQueueTask extends CoreTask {

    @Override
    public boolean run(Context context) {
        return true;
    }

    @Override
    public int priority() {
        return MAX_PRIORITY;
    }
}
