package org.objectscape.wilco.util;

import org.objectscape.wilco.Queue;

import java.util.function.Consumer;

/**
 * Created by plohmann on 27.05.2015.
 */
public class ChanelSelectQueueConsumerPair<T> extends QueueConsumerPair {

    public ChanelSelectQueueConsumerPair(Queue queue, Consumer<T> consumer) {
        super(queue, consumer);
    }

    public void accept(Queue channelQueue, T item) {
        if(queue == channelQueue) {
            queue.execute(() -> consumer.accept(item));
        } else {
            channelQueue.execute(() -> consumer.accept(item));
        }
    }
}
