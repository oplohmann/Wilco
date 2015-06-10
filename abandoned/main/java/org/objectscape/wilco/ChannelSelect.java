package org.objectscape.wilco;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Nutzer on 22.05.2015.
 */
public class ChannelSelect {

    final private Wilco wilco;
    final private Queue queue;

    public ChannelSelect(Wilco wilco, Queue queue) {
        this.wilco = wilco;
        this.queue = queue;
        queue.resume();
    }

    public <T> ChannelSelect onCase(Channel<T> channel, Consumer<T> runnable) {
        channel.onCase(queue, runnable);
        return this;
    }

    public <T> ChannelSelect onTimeout(long timeoutPeriod, TimeUnit unit, Runnable runnable) {
        return this;
    }

    public void clearTimeout() {

    }
}
