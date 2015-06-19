package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.QueueCore;
import org.objectscape.wilco.core.ScheduledRunnable;

/**
 * Created by plohmann on 19.06.2015.
 */
public class CompletedTask extends QueueCoreTask {

    public CompletedTask(QueueCore queueCore, ScheduledRunnable scheduledRunnable) {
        super(queueCore, scheduledRunnable);
    }

    @Override
    public boolean run(Context context) {
        ScheduledRunnable currentRunnable = queueCore.removeCurentTask();
        assert currentRunnable != null;
        assert scheduledRunnable == currentRunnable;

        executeNext(context);

        return true;
    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void clear() {

    }
}
