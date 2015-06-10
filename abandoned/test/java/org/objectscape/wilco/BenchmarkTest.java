package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nutzer on 24.03.2015.
 */
public class BenchmarkTest {

    @Ignore
    @Test
    public void basicBenchmark() throws InterruptedException
    {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        Config config = new Config(availableProcessors * 4, availableProcessors * 8, 500, TimeUnit.MILLISECONDS);
        Wilco wilco = Wilco.newInstance(config);

        int numOfQueues = 10;
        Queue[] queues = new Queue[numOfQueues];

        for (int i = 0; i < numOfQueues; i++) {
            queues[i] = wilco.createQueue();
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
