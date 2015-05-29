package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.tasks.util.IdleInfo;
import org.objectscape.wilco.util.QueueAnchorPair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by plohmann on 29.05.2015.
 */
public class DetectIdleTask extends CoreTask {

    final private long timeoutPeriodInMillis;
    final private Consumer<IdleInfo> idleCallback;
    final private Map<String, QueueAnchorPair> queuesById;

    public DetectIdleTask(long timeoutPeriodInMillis, Map<String, QueueAnchorPair> queuesById, Consumer<IdleInfo> idleCallback) {
        this.timeoutPeriodInMillis = timeoutPeriodInMillis;
        this.idleCallback = idleCallback;
        this.queuesById = queuesById;
    }

    @Override
    public boolean run(Context context) {
        if(System.currentTimeMillis() - context.getLastTimeActive() > timeoutPeriodInMillis) {
            Set<String> suspendedQueues = new HashSet<>();
            Set<String> idleQueues = new HashSet<>();
            if(queuesById.isEmpty()) {
                invokeCallback(context, context.getLastTimeActive(), suspendedQueues, idleQueues, queuesById.size());
                return true;
            }


            for(QueueAnchorPair queueAnchorPair : queuesById.values()) {
                if(!queueAnchorPair.getAnchor().isIdle()) {
                    return true;
                }
                if (queueAnchorPair.getAnchor().isSuspended()) {
                    suspendedQueues.add(queueAnchorPair.getId());
                } else {
                    idleQueues.add(queueAnchorPair.getId());
                }
            }

            invokeCallback(context, context.getLastTimeActive(), suspendedQueues, idleQueues, queuesById.size());
        }

        return true;
    }

    private void invokeCallback(Context context, long noActivitySince, Set<String> suspendedQueuesIds, Set<String> idleQueuesIds, long totalQueuesCount) {
        // don't block the scheduler thread
        context.getExecutor().execute(() -> {
            idleCallback.accept(new IdleInfo(context.getLastTimeActive(), suspendedQueuesIds, idleQueuesIds, totalQueuesCount));
        });
    }

    @Override
    public int priority() {
        return MAX_PRIORITY;
    }

}
