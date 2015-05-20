package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.ShutdownTimeout;
import org.objectscape.wilco.core.tasks.util.ShutdownTaskInfo;
import org.objectscape.wilco.util.QueueAnchorPair;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Nutzer on 20.05.2015.
 */
public class TryRunEmptyShutdownTask extends RunEmptyShutdownTask {

    final private Consumer<ShutdownTimeout> shutdownTimeout;
    final private int trialCount;

    public TryRunEmptyShutdownTask(ShutdownTaskInfo shutdownTaskInfo, List<QueueAnchorPair> allQueues, Consumer<ShutdownTimeout> shutdownTimeout, int trialCount) {
        super(shutdownTaskInfo, allQueues);
        this.shutdownTimeout = shutdownTimeout;
        this.trialCount = trialCount;
    }

    @Override
    protected ShutdownSchedulerTask getShutdownSchedulerTask(List<QueueAnchorPair> nonEmptyQueues) {
        return new TryShutdownSchedulerTask(shutdownTaskInfo, nonEmptyQueues, shutdownTimeout, trialCount, false);
    }
}
