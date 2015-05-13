package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectscape.wilco.core.ShutdownException;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by plohmann on 12.05.2015.
 */
public class ShutdownTest extends AbstractTest {

    @Test
    public void basicShutdown() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Integer> numberOfRunningTasksAfterShutdown = wilco.shutdown(5, TimeUnit.SECONDS);
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = numberOfRunningTasksAfterShutdown.get();
        Assert.assertEquals(0, returnValue);
    }

    @Test(expected = ShutdownException.class)
    public void onlySingleShutdown() {
        wilco.shutdown();
        wilco.shutdown();
    }

    @Test(expected = ShutdownException.class)
    public void cannotCreateQueueAfterShutdown() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Integer> numberOfRunningTasksAfterShutdown = wilco.shutdown(5, TimeUnit.SECONDS);
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = numberOfRunningTasksAfterShutdown.get();
        Assert.assertEquals(0, returnValue);
        wilco.createQueue();
    }

    @Ignore
    @Test
    public void basicShutdownManyTasks() throws InterruptedException, ExecutionException, TimeoutException {
        AtomicInteger counter = new AtomicInteger(0);
        Runnable task = ()-> counter.incrementAndGet();
        int numTasks = 10;

        Queue queue = wilco.createQueue();

        for (int i = 0; i < numTasks; i++) {
            queue.execute(task);
        }

        Thread.sleep(10);
        System.out.println(counter.get());

        CompletableFuture<Integer> numberOfRunningTasksAfterShutdown = wilco.shutdown(2, TimeUnit.SECONDS);
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = numberOfRunningTasksAfterShutdown.get();
        Assert.assertEquals(0, returnValue);
        Assert.assertEquals(numTasks, counter.get());
    }

    @Ignore
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

        CompletableFuture<Integer> numberOfRunningTasksAfterShutdown = wilco.shutdown(2, TimeUnit.SECONDS);
        latch.countDown();
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        int returnValue = numberOfRunningTasksAfterShutdown.get();
        Assert.assertEquals(1, returnValue);

        // retry, now the task should have disappeared as the latch was signaled
        numberOfRunningTasksAfterShutdown = wilco.shutdown(2, TimeUnit.SECONDS);
        returnValue = numberOfRunningTasksAfterShutdown.get();
        Assert.assertEquals(0, returnValue);
    }

    @Before
    public void startUp() {
        super.startUp();
        shutdown = false;
    }
}
