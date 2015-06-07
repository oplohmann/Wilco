package org.objectscape.wilco.util;

import org.objectscape.wilco.OnReceiveConsumer;
import org.objectscape.wilco.Queue;

import java.util.function.Consumer;

/**
 * Created by plohmann on 27.05.2015.
 */
public class ChannelSelectOnReceiveConsumer<T> extends OnReceiveConsumer<T> {

    public ChannelSelectOnReceiveConsumer(Queue channelSelectQueue, Consumer<T> consumer) {
        super(channelSelectQueue, consumer);
    }

    @Override
    public void acceptDeferred(Queue channelQueue, T item) {
        if(queue == channelQueue) {
            accept(item);
        } else {
            queue.execute(() -> consumer.accept(item));
        }
    }
    
}
