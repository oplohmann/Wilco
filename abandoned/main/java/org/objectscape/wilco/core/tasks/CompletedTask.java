package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.ScheduledRunnable;

/**
 * Created by plohmann on 19.02.2015.
 */
public class CompletedTask extends QueueAnchorTask {

    public CompletedTask(QueueAnchor queueAnchor, ScheduledRunnable nextRunnable) {
        super(queueAnchor, nextRunnable);
    }

    @Override
    public boolean run(Context context)
    {
        ScheduledRunnable previousRunnable = queueAnchor.removeCurrentTask();
        assert previousRunnable == scheduledRunnable;

        runWhenDone(context);

        ScheduledRunnable nextRunnable = queueAnchor.peekNextTask();
        if(nextRunnable != null) {
            executeNext(context,  nextRunnable);
        }

        setLastTimeActive(context);
        return true;
    }

    @Override
    public int priority() {
        return MEDIUM_PRIORITY;
    }
}
