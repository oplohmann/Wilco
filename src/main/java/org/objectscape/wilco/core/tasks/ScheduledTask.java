package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.ScheduledRunnable;

import java.util.Optional;

/**
 * Created by plohmann on 19.02.2015.
 */
public class ScheduledTask extends QueueAnchorTask {

    public ScheduledTask(QueueAnchor queueAnchor, Runnable userRunnable) {
        super(queueAnchor, userRunnable);
    }

    public ScheduledTask(QueueAnchor queueAnchor, Runnable userRunnable, Runnable whenDoneRunnable) {
        super(queueAnchor, userRunnable, whenDoneRunnable);
    }

    @Override
    public boolean run(Context context) {
        if(queueAnchor.addTask(scheduledRunnable)) {
            executeNext(context, scheduledRunnable);
        } else {
            clear();
        }
        return true;
    }

    @Override
    public Optional<Runnable> getUserRunnable() {
        return Optional.of(scheduledRunnable.getRunnable());
    }

    @Override
    protected CoreTask createCompletedTask(ScheduledRunnable nextRunnable, Integer taskId) {
        return null;
    }
}
