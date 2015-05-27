package org.objectscape.wilco.util;

import org.objectscape.wilco.Queue;

import java.util.function.Consumer;

/**
 * Created by plohmann on 27.05.2015.
 */
public class ChannelSelectQueueConsumerPair<T> extends QueueConsumerPair<T> {

    final private Queue channelSelectQueue;

    public ChannelSelectQueueConsumerPair(Queue queue, Queue channelSelectQueue, Consumer<T> consumer) {
        super(queue, consumer);
        this.channelSelectQueue = channelSelectQueue;
    }

    @Override
    public void accept(Queue channelQueue, T item) {
        if(queue == channelSelectQueue) {
            queue.execute(() -> consumer.accept(item));
        } else {
            channelSelectQueue.execute(() -> consumer.accept(item));
        }
    }
}
