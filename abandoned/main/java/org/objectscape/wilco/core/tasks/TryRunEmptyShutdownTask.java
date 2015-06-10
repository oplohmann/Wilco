package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.QueueSpine;
import org.objectscape.wilco.core.ShutdownTimeout;
import org.objectscape.wilco.core.tasks.util.ShutdownTaskInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Nutzer on 20.05.2015.
 */
public class TryRunEmptyShutdownTask extends RunEmptyShutdownTask {

    final private Consumer<ShutdownTimeout> shutdownTimeout;
    final private int trialCount;

    public TryRunEmptyShutdownTask(ShutdownTaskInfo shutdownTaskInfo, List<QueueSpine> allQueues, Consumer<ShutdownTimeout> shutdownTimeout, int trialCount) {
        super(shutdownTaskInfo, allQueues);
        this.shutdownTimeout = shutdownTimeout;
        this.trialCount = trialCount;
    }

    @Override
    protected ShutdownSchedulerTask getShutdownSchedulerTask(List<QueueSpine> nonEmptyQueues) {
        return new TryShutdownSchedulerTask(shutdownTaskInfo, nonEmptyQueues, shutdownTimeout, trialCount, false);
    }
}
