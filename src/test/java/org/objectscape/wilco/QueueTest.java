package org.objectscape.wilco;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectscape.wilco.core.DuplicateIdException;
import org.objectscape.wilco.core.QueueClosedException;
import org.objectscape.wilco.core.ShutdownException;
import org.objectscape.wilco.core.dlq.DeadLetterEntry;
import org.objectscape.wilco.core.dlq.DeadLetterListener;
import org.objectscape.wilco.core.tasks.util.ShutdownResponse;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Created by plohmann on 07.05.2015.
 */

public class QueueTest extends AbstractTest {

    @Test
    public void createInstance() {
        Wilco dispatch = Wilco.newInstance(new Config());
    }

    @Test
    public void createChannel() throws InterruptedException, ExecutionException {

        Queue queue = wilco.createQueue();

        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        StringBuffer resultBuffer = new StringBuffer();
        String endToken = "end";

        int numJobs = 5;
        for (int i = 0; i < numJobs; i++) {
            queue.execute(() -> {
                resultBuffer.append(count.incrementAndGet() + " ");
            });
        }

        queue.execute(() -> {
            resultBuffer.append(endToken);
            latch.countDown();
        });

        latch.await();

        Assert.assertEquals(count.get(), numJobs);

        StringBuffer expectedBuffer = new StringBuffer();
        for (int i = 0; i < numJobs; i++) {
            expectedBuffer.append((i + 1) + " ");
        }
        expectedBuffer.append(endToken);

        // for eample if numJobs = 5
        // resultBuffer:   "1 2 3 4 5 end"
        // expectedBuffer: "1 2 3 4 5 end"
        Assert.assertEquals(expectedBuffer.toString(), resultBuffer.toString());

        wilco.shutdown(10, TimeUnit.SECONDS);
    }

    @Test
    public void channelSize() throws InterruptedException {

        Queue queue = wilco.createQueue();

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch blockMainThread = new CountDownLatch(1);
        CountDownLatch taskCompletedLatch = new CountDownLatch(1);

        Assert.assertEquals(queue.size(), 0);

        Runnable runnable = () -> {
            try {
                blockMainThread.countDown();
                latch.await();
            } catch (InterruptedException e) {
                Assert.assertNotNull(e);
            }
        };

        Runnable whenDoneRunnable = () -> {
            taskCompletedLatch.countDown();
        };

        queue.execute(runnable, whenDoneRunnable);

        blockMainThread.await();

        Assert.assertEquals(queue.size(), 1);
        latch.countDown();

        taskCompletedLatch.await();
        Assert.assertEquals(queue.size(), 0);

        wilco.shutdown(10, TimeUnit.SECONDS);
    }

    @Test
    public void createQueue() {
        Queue queue = wilco.createQueue();
        wilco.shutdown(10, TimeUnit.SECONDS);
    }

    @Test(expected = ShutdownException.class)
    public void createChannelAfterShutdown() {
        wilco.shutdown();
        shutdown = false;
        wilco.createChannel();
    }

    @Test
    public void queueSuspendResume() throws InterruptedException {
        Queue queue = wilco.createQueue();

        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger executionCount = new AtomicInteger(0);

        Assert.assertEquals(queue.size(), 0);

        queue.suspend();

        queue.execute(() -> executionCount.incrementAndGet(), () -> latch.countDown());
        queue.execute(()-> executionCount.incrementAndGet(), ()-> latch.countDown());

        Thread.sleep(100); // wait till tasks have ended up in task queue
        Assert.assertEquals(executionCount.get(), 0);
        Assert.assertEquals(queue.size(), 2);

        queue.resume();

        latch.await();

        Thread.sleep(100);
        Assert.assertEquals(executionCount.get(), 2);
        Assert.assertEquals(queue.size(), 0);
        wilco.shutdown(10, TimeUnit.SECONDS);
    }

    @Test
    public void closeChannel() throws InterruptedException {

        Queue queue = wilco.createQueue();

        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        queue.execute(() -> {
            count.incrementAndGet();
            latch.countDown();
        });

        latch.await();

        queue.close();

        boolean queueClosedExceptionThrown = false;

        try {
            queue.execute(() -> count.incrementAndGet());
        }
        catch (QueueClosedException e) {
            queueClosedExceptionThrown = true;
        }

        Assert.assertTrue(queueClosedExceptionThrown);
        Assert.assertEquals(count.get(), 1); // 1 and not 2

        wilco.shutdown(10, TimeUnit.SECONDS);
    }

    @Test
    public void multipleCloseChannelConcurrent() throws InterruptedException {
        Queue queue = wilco.createQueue();

        int loops = 20;
        CountDownLatch latch = new CountDownLatch(loops);
        AtomicInteger queueClosedExceptionCount = new AtomicInteger(0);

        Runnable closeQueue = ()-> {
            try {
                queue.close();
            } catch (Exception e) {
                queueClosedExceptionCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        };

        for (int i = 0; i < loops; i++) {
            new Thread(closeQueue).start();
        }

        latch.await(5, TimeUnit.SECONDS);
        Assert.assertEquals(1, loops - queueClosedExceptionCount.get());
    }

    @Test
    @Ignore // only run manually to see whether Queue.finalize() works
    public void finalizeQueues() throws InterruptedException {
        int numOfQueues = 100;
        Queue[] queues = new Queue[numOfQueues];
        for (int i = 0; i < numOfQueues; i++) {
            queues[0] = wilco.createQueue();
        }

        System.gc();
        Thread.sleep(5000);
    }


    @Test
    public void registerDeadLetterQueue() throws InterruptedException {
        Queue queue = wilco.createQueue();
        AtomicBoolean deadLetterConsumerCalled = new AtomicBoolean(false);

        Consumer<DeadLetterEntry> deadLetterConsumer = (dle) -> {
            deadLetterConsumerCalled.compareAndSet(false, true);
        };

        DeadLetterListener deadLetterListener = new DeadLetterListener(queue.getId(), ArithmeticException.class, deadLetterConsumer);
        wilco.addDLQListener(deadLetterListener);

        CountDownLatch latch = new CountDownLatch(1);

        Runnable runnable = () -> { int result = 1 / 0; };
        Runnable whenDoneRunnable = ()-> latch.countDown();

        queue.execute(runnable, whenDoneRunnable);

        latch.await();

        Assert.assertTrue(deadLetterConsumerCalled.get());
        Assert.assertEquals(1, wilco.getDLQEntries().size());

        DeadLetterEntry entry = wilco.getDLQEntries().get(0);
        Assert.assertEquals("1", entry.getQueueId().get());
        Assert.assertEquals(ArithmeticException.class, entry.getException().getClass());
        Assert.assertTrue(entry.getStackTrace().startsWith("java.lang.ArithmeticException: / by zero"));

        boolean deadLetterFilterFound = wilco.removeDLQListener(deadLetterListener);
        Assert.assertTrue(deadLetterFilterFound);

        wilco.clearDLQ();
        wilco.shutdown(10, TimeUnit.SECONDS);
    }

    @Test(expected = DuplicateIdException.class)
    public void duplicateIdWithTaskQueues() {
        String id = "foo";
        wilco.createQueue(id);
        wilco.createQueue(id);
    }

    @Test(expected = DuplicateIdException.class)
    public void duplicateIdWithListenableQueues() {
        String id = "foo";
        wilco.createQueue(id);
        wilco.createQueue(id);
    }

    @Test
    public void createAndSendListenerQueue() throws InterruptedException {

        Channel<String> queue = wilco.createChannel();

        CountDownLatch latch = new CountDownLatch(1);
        TransferQueue<String> transferQueue = new LinkedTransferQueue<>();
        int numValues = 1000;
        String anything = "anything";

        queue.onReceive(str -> {
            for (int i = 0; i < numValues; i++) {
                transferQueue.add(String.valueOf(i));
            }
            transferQueue.add(str);
            latch.countDown();
        });

        queue.send(anything);
        boolean timeOut = !latch.await(4000, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeOut);

        Assert.assertEquals(
                numValues + 1, // +1 because of "anything" being added to transferQueue in the end
                transferQueue.size());

        // test that order is retained
        for (int i = 0; i < numValues; i++) {
            Assert.assertEquals(String.valueOf(i), transferQueue.take());
        }

        Assert.assertEquals(anything, transferQueue.take());
    }

    @Test
    public void createAndSendListenerQueueSingleCall() throws InterruptedException {

        AtomicReference<String> value = new AtomicReference();
        CountDownLatch latch = new CountDownLatch(1);

        Channel<String> channel = wilco.createChannel();
        channel.onReceive(str -> {
            value.compareAndSet(null, str);
            latch.countDown();
        });

        String anything = "anything";

        channel.send(anything);
        boolean timeOut = !latch.await(1000, TimeUnit.MILLISECONDS);
        Assert.assertFalse(timeOut);

        Assert.assertEquals(anything, value.get());
    }

    @Test
    public void timeoutDeadChannel() throws InterruptedException, TimeoutException, ExecutionException {
        // TODO this one should not fail but currently reveals a timing problem
        shutdown = false;

        Channel<String> a = wilco.createChannel("a");
        Channel<String> b = wilco.createChannel("b");

        AtomicInteger count = new AtomicInteger();
        a.onReceive(str -> count.incrementAndGet());

        // no receive block defined for channel b.

        globalQueue.execute(() -> a.send("a"));
        globalQueue.execute(() -> b.send("b"));

        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.shutdown(2, TimeUnit.SECONDS);
        int numOfPendingSends = shutdownResponse.get(2, TimeUnit.SECONDS).getNotCompletedQueues().size();
        Assert.assertEquals(1, numOfPendingSends);
        Assert.assertTrue(shutdownResponse.get().getNotCompletedQueuesIds().contains("b"));

        Assert.assertEquals(1, count.get());
    }

    @Test
    public void equals() {
        Queue queue1 = wilco.createQueue();
        Queue queue2 = wilco.createQueue();

        Assert.assertEquals(queue1, queue1);
        Assert.assertEquals(queue2, queue2);
        Assert.assertFalse(queue1.equals(queue2));
        Assert.assertFalse(queue1.getId().equals(queue2.getId()));

        Channel<String> channel1 = wilco.createChannel();
        Channel<String> channel2 = wilco.createChannel();

        Assert.assertEquals(channel1, channel1);
        Assert.assertEquals(channel2, channel2);
        Assert.assertFalse(channel1.equals(channel2));
    }

    @Test
    public void hash() {
        Queue queue1 = wilco.createQueue();
        Queue queue2 = wilco.createQueue();

        Assert.assertFalse(queue1.hashCode() == queue2.hashCode());

        Set<Queue> queues = new HashSet<>();
        queues.add(queue1);
        queues.add(queue2);
        queues.add(queue1);
        queues.add(queue2);

        Assert.assertEquals(2, queues.size());

        Channel<String> channel1 = wilco.createChannel();
        Channel<String> channel2 = wilco.createChannel();

        Assert.assertFalse(channel1.hashCode() == channel2.hashCode());

        Set<Channel> channels = new HashSet<>();
        channels.add(channel1);
        channels.add(channel2);
        channels.add(channel1);
        channels.add(channel2);
    }

    @Before
    public void startUp() {
        super.startUp();
        shutdown = false;
    }

}