package org.objectscape.wilco.util;

import org.objectscape.wilco.Queue;

import java.util.function.Consumer;


/**
 * Created by plohmann on 27.05.2015.
 */
public class QueueConsumerPair<T> {

    final protected Queue queue;
    final protected Consumer<T> consumer;

    public QueueConsumerPair(Queue queue, Consumer<T> consumer) {
        this.queue = queue;
        this.consumer = consumer;
    }

    public Queue getQueue() {
        return queue;
    }

    public Consumer<T> getConsumer() {
        return consumer;
    }

    public void accept(Queue channelQueue, T item) {
        queue.execute(()-> consumer.accept(item));
    }
}