package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.QueueCore;
import org.objectscape.wilco.core.ScheduledRunnable;

/**
 * Created by plohmann on 19.06.2015.
 */
public class ScheduledTask extends QueueCoreTask {

    public ScheduledTask(QueueCore queueCore, ScheduledRunnable scheduledRunnable) {
        super(queueCore, scheduledRunnable);
    }

    @Override
    public boolean run(Context context) {
        if(queueCore.addTask(scheduledRunnable)) {
            executeNext(context);
        }
        return true;
    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void clear() {
        queueCore = null;
        scheduledRunnable = null;
    }
}
