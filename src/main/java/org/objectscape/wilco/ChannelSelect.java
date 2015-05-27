package org.objectscape.wilco;

import java.util.function.Consumer;

/**
 * Created by Nutzer on 22.05.2015.
 */
public class ChannelSelect {

    final private Wilco wilco;
    final private Queue queue;

    public ChannelSelect(Wilco wilco) {
        this.wilco = wilco;
        queue = wilco.createQueue();
    }

    public <T> ChannelSelect onCase(Channel<T> channel, Consumer<T> runnable) {
        channel.onCase(queue, runnable);
        return this;
    }
}
