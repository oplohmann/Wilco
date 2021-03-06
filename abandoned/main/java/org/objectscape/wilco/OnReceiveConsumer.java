package org.objectscape.wilco;

import java.util.function.Consumer;


/**
 * Created by plohmann on 27.05.2015.
 */
public class OnReceiveConsumer<T> {

    final protected Queue queue;
    final protected Consumer<T> consumer;

    public OnReceiveConsumer(Queue queue, Consumer<T> consumer) {
        this.queue = queue;
        this.consumer = consumer;
    }

    public Queue getQueue() {
        return queue;
    }

    public Consumer<T> getConsumer() {
        return consumer;
    }

    public void accept(T item) {
        queue.executeIgnoreCloseUser(() -> consumer.accept(item));
    }

    public void acceptDeferred(Queue channelQueue, T item) {
        accept(item);
    }
}
