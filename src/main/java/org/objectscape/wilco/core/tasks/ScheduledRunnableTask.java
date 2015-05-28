package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.ScheduledRunnable;

import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Nutzer on 25.05.2015.
 */
public abstract class ScheduledRunnableTask extends CoreTask {

    protected ScheduledRunnable scheduledRunnable;

    public ScheduledRunnableTask() {
    }

    public ScheduledRunnableTask(ScheduledRunnable scheduledRunnable) {
        this.scheduledRunnable = scheduledRunnable;
    }

    protected void runWhenDone(Context context) {
        if(scheduledRunnable != null) {
            try {
                scheduledRunnable.runWhenDone();
            } catch (Exception e) {
                context.addToDeadLetterQueue(queueId().orElse(null), e);
            }
        }
    }

    protected void executeNext(final Context context, ScheduledRunnable nextRunnable) {
        context.getExecutor().execute(() -> {
            try {
                nextRunnable.run();
            }
            catch(RejectedExecutionException e) {
                // TODO - not yet implemented
            }
            catch (Throwable throwable) {
                context.addToDeadLetterQueue(queueId().orElse(null), throwable);
            }
            context.getEntryQueue().add(createCompletedTask(nextRunnable));
            clear();
        });
    }

    protected void setLastTimeActive(Context context) {
        context.setLastTimeActive(System.currentTimeMillis());
    }

    protected void executeNext(final Context context, ScheduledRunnable nextRunnable, Integer taskId) {
        context.getExecutor().execute(() -> {
            try {
                nextRunnable.run();
            }
            catch(RejectedExecutionException e) {
                // TODO - not yet implemented
            }
            catch (Throwable throwable) {
                context.addToDeadLetterQueue(queueId().orElse(null), throwable);
            }
            context.getEntryQueue().add(createCompletedTask(nextRunnable, taskId));
            clear();
        });
    }

    protected abstract CoreTask createCompletedTask(ScheduledRunnable nextRunnable, Integer taskId);

    protected abstract CoreTask createCompletedTask(ScheduledRunnable nextRunnable);

    protected void clear() {
        // make sure this instance does not cling to long-lived objects
        scheduledRunnable = null;
    }

    @Override
    public int priority() {
        return MEDIUM_PRIORITY;
    }

}
