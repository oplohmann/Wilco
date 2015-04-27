package org.objectscape.wilco;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by plohmann on 22.04.2015.
 */
public class Channel<T> {

    final private static Random Randomizer = new Random(System.currentTimeMillis());

    final private Queue queue;
    private List<Consumer<T>> consumers = new ArrayList();
    private Runnable onClose;
    final private Object onCloseLock = new Object();
    final private Object consumersLock = new Object();
    final private CompletableFuture<T> closedFuture = new CompletableFuture();
    private T closeValue;
    final private List<T> buffer = new ArrayList();

    public Channel(Queue queue) {
        this.queue = queue;
    }

    public void send(T item) {
        synchronized (consumersLock) {
            if(!consumers.isEmpty()) {
                queue.execute(() -> {
                    getNextConsumer().accept(item);
                });
            }
            else {
                buffer.add(item);
            }
        }
    }

    private Consumer<T> getNextConsumer() {
        if(consumers.size() == 1) {
            return consumers.get(0);
        }
        return consumers.get(Randomizer.nextInt(consumers.size()));
    }

    public CompletableFuture<T> onReceive(Consumer<T> consumer) {
        synchronized (consumersLock) {
            consumers.add(consumer);
            if(buffer.isEmpty()) {
                return closedFuture;
            }
            for(T item : buffer) {
                queue.execute(() -> {
                    getNextConsumer().accept(item);
                });
            }
            buffer.clear();
            return closedFuture;
        }
    }

    public void close(T closeValue) {
        queue.close();
        synchronized (onCloseLock) {
            this.closeValue = closeValue;
            closedFuture.complete(closeValue);
            if(onClose != null) {
                queue.executeOnClose(() -> onClose.run());
            }
        }
    }

    public void onClose(Runnable onClose) {
        synchronized (onCloseLock) {
            if(this.onClose != null) {
                throw new RuntimeException("on close callback already defined");
            }
            this.onClose = onClose;
            if(closeValue != null) {
                queue.executeOnClose(() -> onClose.run());
            }
        }
    }

}
