package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Test;
import org.objectscape.wilco.core.IdleException;
import org.objectscape.wilco.core.tasks.util.IdleInfo;
import org.objectscape.wilco.util.Ref;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by plohmann on 28.05.2015.
 */
public class WilcoTest extends AbstractTest {

    @Test
    public void onIdle() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger();
        long period = 100;

        ScheduledFuture future = wilco.onIdleAfter(period, TimeUnit.MILLISECONDS, (IdleInfo info) -> {
            count.incrementAndGet();
            if(count.get() > 2) {
                latch.countDown();
            }
        });

        boolean noTimeout = latch.await(2 * period, TimeUnit.SECONDS);
        future.cancel(false);
        Assert.assertTrue(noTimeout);
        Assert.assertEquals(3, count.get());
    }

    @Test
    public void duplicateOnIdle() throws InterruptedException, ExecutionException {

        ScheduledFuture future = wilco.onIdleAfter(1L, TimeUnit.SECONDS, (IdleInfo info) -> { });

        boolean idleExceptionThrown = false;

        try {
            wilco.onIdleAfter(1L, TimeUnit.SECONDS, (IdleInfo info) -> { });
        } catch (IdleException e) {
            idleExceptionThrown = true;
        }

        Assert.assertTrue(idleExceptionThrown);

        future.cancel(true);

        idleExceptionThrown = false;

        try {
            wilco.onIdleAfter(1L, TimeUnit.SECONDS, (IdleInfo info) -> { });
        } catch (IdleException e) {
            idleExceptionThrown = true;
        }

        Assert.assertFalse(idleExceptionThrown);
    }

    @Test
    public void onIdleSeveralQueues() throws InterruptedException, ExecutionException {
        long period = 100;
        int taskCount = 5;

        String suspendedQueueName = "suspendedQueue";
        Queue suspendedQueue = wilco.createQueue(suspendedQueueName);
        suspendedQueue.suspend();

        String queueName = "queue";
        Queue queue = wilco.createQueue(queueName);

        Runnable runnable = () -> {
            try {
                Thread.sleep(period);
            } catch (InterruptedException e) { }
        };

        long start = System.currentTimeMillis();

        for (int i = 0; i < taskCount; i++) {
            queue.execute(runnable);
        }

        Ref<IdleInfo> idleInfo = new Ref<>();
        CountDownLatch latch = new CountDownLatch(1);

        ScheduledFuture future = wilco.onIdleAfter(period, TimeUnit.MILLISECONDS, (IdleInfo info) -> {
            idleInfo.set(info);
            latch.countDown();
        });

        boolean noTimeout = latch.await(taskCount * period * 2, TimeUnit.SECONDS);
        Assert.assertTrue(noTimeout);
        future.cancel(true);

        Assert.assertTrue(idleInfo.get().getIdleQueuesIds().contains(queueName));
        Assert.assertTrue(idleInfo.get().getSuspendedQueuesIds().contains(suspendedQueueName));
        Assert.assertEquals(2 + 1, idleInfo.get().getTotalQueuesCount()); // +1 beacuse of internal AsyncQueue
        Assert.assertTrue(idleInfo.get().getNoActivitySince() >= start + period * taskCount);
    }
}
