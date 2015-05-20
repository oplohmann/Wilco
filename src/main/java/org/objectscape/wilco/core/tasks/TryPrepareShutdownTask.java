package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.ShutdownTimeout;
import org.objectscape.wilco.core.tasks.util.ShutdownTaskInfo;
import org.objectscape.wilco.util.QueueAnchorPair;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Nutzer on 20.05.2015.
 */
public class TryPrepareShutdownTask extends PrepareShutdownTask {

    final private Consumer<ShutdownTimeout> shutdownTimeout;
    final private int trialCount;
    final private List<QueueAnchorPair> nonEmptyQueues;

    public TryPrepareShutdownTask(String wilco, ShutdownTaskInfo shutdownTaskInfo, Consumer<ShutdownTimeout> shutdownTimeout, List<QueueAnchorPair> nonEmptyQueues, int trialCount) {
        super(wilco, shutdownTaskInfo);
        this.shutdownTimeout = shutdownTimeout;
        this.trialCount = trialCount;
        this.nonEmptyQueues = nonEmptyQueues;
    }

    protected RunEmptyShutdownTask getRunEmptyShutdownTask() {
        List<QueueAnchorPair> tempNonEmptyQueues = nonEmptyQueues;
        if(trialCount == 1) {
            tempNonEmptyQueues = getCore().closeAllQueues();
        }
        return new TryRunEmptyShutdownTask(shutdownTaskInfo, tempNonEmptyQueues, shutdownTimeout, trialCount);
    }

}
