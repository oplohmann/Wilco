package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.ShutdownTimeout;
import org.objectscape.wilco.core.tasks.util.ShutdownResponse;
import org.objectscape.wilco.core.tasks.util.ShutdownTaskInfo;
import org.objectscape.wilco.util.QueueAnchorPair;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Nutzer on 20.05.2015.
 */
public class TryShutdownSchedulerTask extends ShutdownSchedulerTask {

    final private Consumer<ShutdownTimeout> shutdownTimeout;
    final int trialCount;
    final boolean shutdownNow;

    public TryShutdownSchedulerTask(ShutdownTaskInfo shutdownTaskInfo, List<QueueAnchorPair> nonEmptyQueues, Consumer<ShutdownTimeout> shutdownTimeout, int trialCount, boolean shutdownNow) {
        super(shutdownTaskInfo, nonEmptyQueues);
        this.shutdownTimeout = shutdownTimeout;
        this.trialCount = trialCount;
        this.shutdownNow = shutdownNow;
    }

    @Override
    public boolean run(Context context) {
        if(shutdownNow) {
            return executeShutdownNow(context);
        }
        if(nonEmptyQueues.isEmpty()) {
            return executeShutdown(context);
        }
        invokeShutdownTimeout(context);
        return true;
    }

    private boolean executeShutdown(Context context) {
        long remainingShutdownTime = getDurationInMillis() - (System.currentTimeMillis() - getStart());
        context.getExecutor().shutdown();
        if(remainingShutdownTime > 0) {
            try {
                context.getExecutor().awaitTermination(remainingShutdownTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                context.addToDeadLetterQueue(null, e);
            }
        }
        getCore().commitShutdown();
        getDoneSignal().complete(createShutdownResponse());

        return false;
    }

    private boolean executeShutdownNow(Context context) {
        context.getExecutor().shutdownNow();
        getCore().commitShutdown();
        getDoneSignal().complete(createShutdownResponse());
        return false;
    }

    protected boolean invokeShutdownTimeout(Context context) {
        shutdownTimeout.accept(new ShutdownTimeout(getCore(), getDoneSignal(), createShutdownResponse(), nonEmptyQueues, shutdownTimeout, trialCount));
        return false;
    }

    protected ShutdownResponse finalShutdownThreadPoolAndScheduler(Context context) {
        return null;
    }

}
