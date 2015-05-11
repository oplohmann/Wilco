package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by plohmann on 11.05.2015.
 */
public class WilcoTest extends AbstractTest {

    @Test
    public void shutdown() {
        int numCounterQueues = 10;
        int counterJobsPerQueue = 100;
        Queue[] counterQueues = new Queue[numCounterQueues];
        AtomicInteger counter = new AtomicInteger(0);

        Queue blockingTaskQueue = wilco.createQueue();
        CountDownLatch blockingLatch = new CountDownLatch(1);
        blockingTaskQueue.execute(()-> {
            try {
                blockingLatch.await();
            } catch (InterruptedException e) {
                Assert.assertNotNull(e);
            }
        });

        Runnable counterRunnable = ()-> {
            counter.incrementAndGet();
        };

        for (int i = 0; i < numCounterQueues; i++) {
            counterQueues[i] = wilco.createQueue();
            for (int j = 0; j < counterJobsPerQueue; j++) {
                counterQueues[i].execute(counterRunnable);
            }
        }

        wilco.shutdown(1000, TimeUnit.SECONDS);
    }

    protected boolean isShutdown() {
        return false;
    }
}
