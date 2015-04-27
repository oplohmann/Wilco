package org.objectscape.wilco;

import org.fusesource.hawtdispatch.Dispatch;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Nutzer on 24.03.2015.
 */
public class HawtDispatchBenchmarkTest {

    @Ignore
    @Test
    public void basicBenchmark() throws InterruptedException
    {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int numOfQueues = 10;
        DispatchQueue[] queues = new DispatchQueue[numOfQueues];

        for (int i = 0; i < numOfQueues; i++) {
            queues[i] = Dispatch.createQueue();
        }

        int numOfTasksPerQueue = 10_000;
        int numOfTasks = numOfQueues * numOfTasksPerQueue;
        int delayInMillis = 5;

        CountDownLatch latch = new CountDownLatch(numOfTasks);
        long start = System.currentTimeMillis();

        for (int i = 0; i < numOfTasksPerQueue; i++) {
            for (int j = 0; j < numOfQueues; j++) {
                queues[j].execute(()-> {
                    try {
                        Thread.sleep(delayInMillis);
                    } catch (InterruptedException e) {
                        Assert.assertNotNull(e);
                    }
                    latch.countDown();
                });
            }
        }

        latch.await();

        System.out.println("duration: " + (System.currentTimeMillis() - start) + " millis");
    }

}
