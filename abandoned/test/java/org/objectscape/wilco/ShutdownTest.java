package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectscape.wilco.core.ShutdownException;
import org.objectscape.wilco.core.tasks.util.ShutdownResponse;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by plohmann on 12.05.2015.
 */
public class ShutdownTest extends AbstractTest {

    @Test
    public void basicShutdown() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.shutdown(5, TimeUnit.SECONDS);
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = shutdownResponse.get().getNotCompletedQueues().size();
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
        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.shutdown(5, TimeUnit.SECONDS);
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = shutdownResponse.get().getNotCompletedQueues().size();
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

        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.shutdown(totalTaskExecutionDuration * 2, TimeUnit.MILLISECONDS);
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = shutdownResponse.get().getNotCompletedQueues().size();
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

        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.shutdown(2, TimeUnit.SECONDS);
        shutdownResponse.get(); // wait till shutdown has completed
        latch.countDown();
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = shutdownResponse.get().getNotCompletedQueues().size();
        Assert.assertEquals(1, returnValue);
        Assert.assertFalse(shutdownResponse.get().isShutdownCompleted());

        // wilco.shutdown really returned the same Runnable as not finished when shutting down?
        Assert.assertTrue(shutdownResponse.get().getNotCompletedRunnables().iterator().next() == task);
    }

    @Test
    public void tryShutdownCannotComplete() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger();

        Runnable task = ()-> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Assert.assertNotNull(e);
            }
        };

        String queueId = "myQueue";
        Queue queue = wilco.createQueue(queueId);
        queue.execute(task);

        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.tryShutdown(1, TimeUnit.SECONDS,
            (shutdownTimeout -> {
                counter.incrementAndGet();
                Assert.assertTrue(shutdownTimeout.getNotCompletedQueuesIds().contains(queueId));
                Assert.assertTrue(shutdownTimeout.getNotCompletedRunnables().iterator().next() == task);
                if (shutdownTimeout.tryCount() <= 2) {
                    shutdownTimeout.retryShutdown(1, TimeUnit.SECONDS);
                } else {
                    shutdownTimeout.shutdownNow();
                }
        }));

        Assert.assertFalse(shutdownResponse.get().isShutdownCompleted());
        Assert.assertEquals(3, counter.get());
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
    }

    @Test
    public void tryShutdownMustComplete() throws InterruptedException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger();

        Runnable task = ()-> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Assert.assertNotNull(e);
            }
        };

        String queueId = "myQueue";
        Queue queue = wilco.createQueue(queueId);
        queue.execute(task);

        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.tryShutdown(1, TimeUnit.SECONDS,
                (shutdownTimeout -> {
                    counter.incrementAndGet();
                    Assert.assertTrue(shutdownTimeout.getNotCompletedQueuesIds().contains(queueId));
                    Assert.assertTrue(shutdownTimeout.getNotCompletedRunnables().iterator().next() == task);
                    if (shutdownTimeout.tryCount() == 1) {
                        shutdownTimeout.retryShutdown(1, TimeUnit.SECONDS);
                    } else {
                        latch.countDown();
                        shutdownTimeout.retryShutdown(1, TimeUnit.SECONDS);
                    }
                }));

        Assert.assertTrue(shutdownResponse.get().isShutdownCompleted());
        Assert.assertEquals(2, counter.get());
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
    }

    @Test
    public void trySimpleShutdown() throws InterruptedException, ExecutionException, TimeoutException {
        AtomicInteger counter = new AtomicInteger();

        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.tryShutdown(
                1, TimeUnit.SECONDS, shutdownTimeout -> counter.incrementAndGet());

        boolean shutdownCompleted = shutdownResponse.get(5, TimeUnit.SECONDS).isShutdownCompleted();
        Assert.assertTrue(shutdownCompleted); // must succeed as no task was executed
    }

    @Before
    public void startUp() {
        super.startUp();
        shutdown = false;
    }
}
