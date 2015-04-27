package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.ScheduledRunnable;

/**
 * Created by plohmann on 25.03.2015.
 */
public class ResumeChannelTask extends QueueAnchorTask {

    public ResumeChannelTask(QueueAnchor anchor) {
        super(anchor);
    }

    public ResumeChannelTask(QueueAnchor queueAnchor, Runnable whenDoneRunnable) {
        super(queueAnchor, whenDoneRunnable);
    }

    @Override
    public boolean run(Context context) {
        queueAnchor.setSuspended(false);
        ScheduledRunnable runnable = queueAnchor.peekNextTask();
        if(runnable != null) {
            executeNext(context, runnable);
        }

        runWhenDone(context);

        if(runnable == null) {
            clear();
        }

        return true;
    }

    @Override
    public int priority() {
        return CoreTask.MAX_PRIORITY;
    }
}
