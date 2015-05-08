package org.objectscape.wilco;

import junit.framework.Assert;
import org.junit.Test;
import org.objectscape.wilco.core.dlq.DeadLetterEntry;
import org.objectscape.wilco.core.dlq.DeadLetterListener;
import org.objectscape.wilco.core.dlq.DeadLetterQueue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by Nutzer on 27.03.2015.
 */
public class DeadLetterLinkedQueueTest {

    @Test
    public void executorWorkbook() throws InterruptedException {
        DeadLetterQueue dlq = new DeadLetterQueue();
        String queueId = "0";

        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        Consumer<DeadLetterEntry> callback = (dle) -> {
            Assert.assertEquals(queueId, dle.getQueueId());
            Assert.assertEquals(ArithmeticException.class, dle.getException().getClass());
            count.incrementAndGet();
            latch.countDown();
        };
        DeadLetterListener listener = new DeadLetterListener(queueId, ArithmeticException.class, callback);
        dlq.addListener(listener);

        Runnable runnable = ()-> {
            try {
                int result = 1/0;
            } catch (Exception e) {
                dlq.add(queueId, e);
            }
        };

        runnable.run();

        boolean noTimeOut = latch.await(1000, TimeUnit.DAYS.MILLISECONDS);
        Assert.assertTrue(noTimeOut);
        Assert.assertEquals(1, count.get());
        boolean found = dlq.removeListener(listener);
        Assert.assertTrue(found);
    }

    @Test
    public void exceptionOverflow() throws InterruptedException {
        int maxSize = 10;
        DeadLetterQueue dlq = new DeadLetterQueue(maxSize);
        String queueId = "0";

        try {
            int result = 1/0;
        } catch (Exception e) {
            for (int i = 0; i < maxSize * maxSize; i++) {
                dlq.add(queueId, e);
            }
        }

        Assert.assertEquals(maxSize, dlq.getDeadLetterEntries().size());

        dlq.clear();
        Assert.assertEquals(0, dlq.getDeadLetterEntries().size());
    }

    @Test
    public void exceptionOutdated() throws InterruptedException {
        int maxSize = 10;
        DeadLetterQueue dlq = new DeadLetterQueue(maxSize, 1, TimeUnit.MILLISECONDS);
        String queueId = "0";

        try {
            int result = 1/0;
        } catch (Exception e) {
            dlq.add(queueId, e);
            Thread.sleep(10);
            dlq.add(queueId, e);
        }

        Assert.assertEquals(1, dlq.getDeadLetterEntries().size());
    }
}
