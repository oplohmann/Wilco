package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.QueueAnchor;

/**
 * Created by plohmann on 25.03.2015.
 */
public class SuspendChannelTask extends QueueAnchorTask
{

    public SuspendChannelTask(QueueAnchor anchor) {
        super(anchor);
    }

    public SuspendChannelTask(QueueAnchor queueAnchor, Runnable whenDoneRunnable) {
        super(queueAnchor, null, whenDoneRunnable);
    }

    @Override
    public boolean run(Context context) {
        queueAnchor.setSuspended(true);
        runWhenDone(context);
        clear();
        return true;
    }

    @Override
    public int priority() {
        return CoreTask.MAX_PRIORITY;
    }
}
