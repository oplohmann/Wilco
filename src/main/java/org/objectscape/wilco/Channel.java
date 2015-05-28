package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueClosedException;
import org.objectscape.wilco.util.ChannelSelectQueueConsumerPair;
import org.objectscape.wilco.util.QueueConsumerPair;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Created by plohmann on 22.04.2015.
 */
public class Channel<T> {

    final private static ThreadLocalRandom Randomizer = ThreadLocalRandom.current();

    final private Queue queue;
    private List<QueueConsumerPair<T>> consumers = new CopyOnWriteArrayList<>();
    private AtomicReference<Runnable> onCloseRef = new AtomicReference<>();
    final private CompletableFuture<T> closedFuture = new CompletableFuture();
    private T closeValue;
    final private AtomicBoolean suspended = new AtomicBoolean(true);
    private int currentSelectedConsumer = -1;

    public Channel(Queue queue) {
        this(queue, Alternation.Random);
    }

    public Channel(Queue queue, Alternation alternationBetweenReceivers) {
        this.queue = queue;
        queue.suspend(); // wait till consumer(s) are added
        if(Alternation.Random.equals(alternationBetweenReceivers)) {
            currentSelectedConsumer = -1;
        } else if(Alternation.Cyclic.equals(alternationBetweenReceivers)) {
            currentSelectedConsumer = 0;
        }
    }

    public void send(T item) {
        QueueConsumerPair<T> queueConsumerPair = getNextConsumerPair();
        if(queueConsumerPair == null) {
            queue.execute(() -> {
                // If no consumers available, the queue remains suspended until some consumer is added.
                // In that case the consumer is evaluated lazily once the queue is resumed.
                getNextConsumer(consumers).acceptDeferred(queue, item);
            });
        }
        else {
            queueConsumerPair.accept(item);
        }
    }

    private QueueConsumerPair<T> getNextConsumerPair() {
        if(consumers.isEmpty()) {
            return null;
        }
        return getNextConsumerPair(consumers);
    }

    private QueueConsumerPair<T> getNextConsumer(List<QueueConsumerPair<T>> consumers) {
        return getNextConsumerPair(consumers);
    }

    private QueueConsumerPair<T> getNextConsumerPair(List<QueueConsumerPair<T>> consumers) {
        assert !consumers.isEmpty();

        // consumers never shrinks, only grows. So this is safe.
        if(consumers.size() == 1) {
            return consumers.get(0);
        }

        if(currentSelectedConsumer != -1) {
            // alternate between consumers in a cyclic way
            if(currentSelectedConsumer == consumers.size()) {
                currentSelectedConsumer = 0;
            };

            int index = currentSelectedConsumer;
            currentSelectedConsumer++;

            return consumers.get(index);
        }

        // undetermined selection of next consumer
        return consumers.get(Randomizer.nextInt(consumers.size()));
    }

    public CompletableFuture<T> onReceive(Consumer<T> consumer) {
        consumers.add(new QueueConsumerPair<T>(queue, consumer));
        receiverAdded();
        return closedFuture;
    }

    public void close(T closeValue) {
        queue.execute(() -> {
            try {
                queue.close();
            } catch (QueueClosedException e) {
                return;
            }
            this.closeValue = closeValue;
            closedFuture.complete(closeValue);
            if (onCloseRef.get() != null) {
                queue.executeIgnoreClose(() -> onCloseRef.get().run());
            }
        });
    }

    public CompletableFuture<T> getClosedFuture() {
        return closedFuture;
    }

    public void close() {
        close(null);
    }

    public void closeAndWaitTillDone() throws ExecutionException, InterruptedException {
        close();
        closedFuture.get();
    }

    public void onClose(Runnable onClose) {
        if(!onCloseRef.compareAndSet(null, onClose)) {
            throw new RuntimeException("on close callback already defined");
        }
        queue.executeIgnoreClose(() -> {
            if (closeValue != null) {
                onCloseRef.get().run();
            }
        });
    }

    public void waitTillClosed() throws ExecutionException, InterruptedException {
        closedFuture.get();
    }

    protected void finalize() throws Throwable {
        try {
            queue.clear();
        } finally {
            super.finalize();
        }
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject)
            return true;
        if (otherObject == null || getClass() != otherObject.getClass())
            return false;

        Channel<?> channel = (Channel<?>) otherObject;

        return queue.equals(channel.queue);

    }

    @Override
    public int hashCode() {
        int result = queue.hashCode();
        result = 31 * result + onCloseRef.hashCode();
        return result;
    }

    protected void onCase(Queue channelSelectQueue, Consumer<T> consumer) {
        consumers.add(new ChannelSelectQueueConsumerPair<>(channelSelectQueue, consumer));
        receiverAdded();
    }

    private void receiverAdded() {
        if(suspended.get() && suspended.compareAndSet(true, false)) {
            queue.resume();
        }
    }

    @Override
    public String toString() {
        return "Channel" + queue.getIdWithCoreId();
    }
}
