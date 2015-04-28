package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueClosedException;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Created by plohmann on 22.04.2015.
 */
public class Channel<T> {

    final private static Random Randomizer = new Random(System.currentTimeMillis());

    final private Queue queue;
    private List<Consumer<T>> consumers = new CopyOnWriteArrayList<>();
    private AtomicReference<Runnable> onCloseRef = new AtomicReference<>();
    final private Object onCloseLock = new Object();
    final private CompletableFuture<T> closedFuture = new CompletableFuture();
    private T closeValue;
    final private AtomicBoolean suspended = new AtomicBoolean(true);

    public Channel(Queue queue) {
        this.queue = queue;
        queue.suspend(); // wait till consumer(s) are added
    }

    public void send(T item) {
        queue.execute(() -> {
            getNextConsumer(consumers).accept(item);
        });
    }

    private Consumer<T> getNextConsumer(List<Consumer<T>> consumers) {
        assert !consumers.isEmpty();
        // Consumers never shrinks, only grows. So this is safe.
        if(consumers.size() == 1) {
            return consumers.get(0);
        }
        return consumers.get(Randomizer.nextInt(consumers.size()));
    }

    public CompletableFuture<T> onReceive(Consumer<T> consumer) {
        consumers.add(consumer);
        if(suspended.get() && suspended.compareAndSet(true, false)) {
            queue.resume();
        }
        return closedFuture;
    }

    public void close(T closeValue) {
        queue.execute(() -> {
            boolean alreadyClosed = false;
            try {
                queue.close();
            } catch (QueueClosedException e) {
                // no non-local returns in Java
                alreadyClosed = true;
            }
            if(alreadyClosed) {
                return;
            }
            this.closeValue = closeValue;
            closedFuture.complete(closeValue);
            if (onCloseRef.get() != null) {
                queue.executeIgnoreClose(() -> onCloseRef.get().run());
            }
        });
    }

    public void onClose(Runnable onClose) {
        if(!onCloseRef.compareAndSet(null, onClose)) {
            throw new RuntimeException("on close callback already defined");
        }
        queue.executeIgnoreClose(() -> {
            if (closeValue != null) {
                onClose.run();
            }
        });
    }

}
