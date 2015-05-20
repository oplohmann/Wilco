package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectscape.wilco.core.ShutdownException;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by plohmann on 12.05.2015.
 */
public class ShutdownTest extends AbstractTest {

    @Test
    public void basicShutdown() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<List<Runnable>> numberOfRunningTasksAfterShutdown = wilco.shutdown(5, TimeUnit.SECONDS);
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = numberOfRunningTasksAfterShutdown.get().size();
        Assert.assertEquals(0, returnValue);
        Assert.assertFalse(wilco.isSchedulerRunning());
    }

    @Test(expected = ShutdownException.class)
    public void onlySingleShutdown() {
        wilco.shutdown();
        wilco.shutdown();
    }

    @Test(expected = ShutdownException.class)
    public void cannotCreateQueueAfterShutdown() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<List<Runnable>> numberOfRunningTasksAfterShutdown = wilco.shutdown(5, TimeUnit.SECONDS);
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = numberOfRunningTasksAfterShutdown.get().size();
        Assert.assertEquals(0, returnValue);
        wilco.createQueue();
    }

    @Test
    public void basicShutdownManyTasks() throws InterruptedException, ExecutionException, TimeoutException {
        AtomicInteger counter = new AtomicInteger(0);
        long taskBusyDurationInMillis = 10;

        Runnable task = ()-> {
            counter.incrementAndGet();
            try {
                Thread.sleep(taskBusyDurationInMillis);
            } catch (InterruptedException e) {
            }
        };

        int numOfQueues = 20;
        Queue[] queues = new Queue[numOfQueues];
        for (int i = 0; i < numOfQueues; i++) {
            queues[i] = wilco.createQueue();
        }

        int numOfTasks = 20;
        for (int i = 0; i < numOfQueues; i++) {
            for (int j = 0; j < numOfTasks; j++) {
                queues[i].execute(task);
            }
        }

        long totalTaskExecutionDuration = numOfQueues * numOfTasks * taskBusyDurationInMillis;

        CompletableFuture<List<Runnable>> numOfRunningTasksAfterShutdown = wilco.shutdown(totalTaskExecutionDuration * 2, TimeUnit.MILLISECONDS);
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = numOfRunningTasksAfterShutdown.get().size();
        Assert.assertEquals(0, returnValue);
        Assert.assertEquals(numOfTasks * numOfQueues, counter.get());
        Assert.assertFalse(wilco.isSchedulerRunning());
    }

    @Test
    public void basicShutdownTimeoutExpired() throws InterruptedException, ExecutionException, TimeoutException {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable task = ()-> {
            try {
                latch.await();
            } catch (InterruptedException e) { }
        };

        Queue queue = wilco.createQueue();
        queue.execute(task);

        CompletableFuture<List<Runnable>> numberOfRunningTasksAfterShutdown = wilco.shutdown(2, TimeUnit.SECONDS);
        numberOfRunningTasksAfterShutdown.get(); // wait till shutdown has completed
        latch.countDown();
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = numberOfRunningTasksAfterShutdown.get().size();
        Assert.assertEquals(1, returnValue);

        // wilco.shutdown really returned the same Runnable as not finished when shutting down?
        Assert.assertTrue(numberOfRunningTasksAfterShutdown.get().get(0) == task);
    }

    @Ignore
    @Test
    public void tryShutdown() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        long taskBusyDurationInMillis = 10;

        Runnable task = ()-> {
            counter.incrementAndGet();
            try {
                Thread.sleep(taskBusyDurationInMillis);
            } catch (InterruptedException e) {
            }
        };

        int numOfQueues = 20;
        Queue[] queues = new Queue[numOfQueues];
        for (int i = 0; i < numOfQueues; i++) {
            queues[i] = wilco.createQueue();
        }

        int numOfTasks = 20;
        for (int i = 0; i < numOfQueues; i++) {
            for (int j = 0; j < numOfTasks; j++) {
                queues[i].execute(task);
            }
        }

        CountDownLatch latch = new CountDownLatch(1);
        long totalTaskExecutionDuration = numOfQueues * numOfTasks * taskBusyDurationInMillis;

        wilco.tryShutdown(
                totalTaskExecutionDuration / 10,
                TimeUnit.MILLISECONDS,
                (shutdownCallback -> {
                    if(shutdownCallback.isShutdownComplete()) {
                        latch.countDown();
                        return;
                    }
                    if(!shutdownCallback.isQueuesRunEmpty()) {
                        if(shutdownCallback.tryCount() == 1) {
                            shutdownCallback.retryShutdown(totalTaskExecutionDuration / 10, TimeUnit.MILLISECONDS);
                        }
                        else {
                            shutdownCallback.getNonEmptyQueuesIds();
                            shutdownCallback.shutdownNow();
                        }
                    }
                }));

        latch.await();
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
    }

    @Before
    public void startUp() {
        super.startUp();
        shutdown = false;
    }
}
