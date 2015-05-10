package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.dlq.DeadLetterEntry;
import org.objectscape.wilco.core.dlq.DeadLetterListener;
import org.objectscape.wilco.core.tasks.CreateQueueTask;
import org.objectscape.wilco.core.tasks.ShutdownTask;
import org.objectscape.wilco.util.IdStore;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Wilco {

    final private IdStore idStore = new IdStore();
    final private WilcoCore core;

    public Wilco() {
        this(new Config());
    }

    public static Wilco newInstance(Config config) {
        return new Wilco(config);
    }

    public Wilco(Config config) {
        super();
        if(config == null) {
            throw new NullPointerException("config null");
        }
        core = new WilcoCore(config);
    }

    public Queue createQueue() {
        return createQueue(idStore.generateId());
    }

    public <T> Channel<T> createChannel() {
        return new Channel<>(createQueue(), Alternation.Random);
    }

    public <T> Channel<T> createChannel(Alternation alternationBetweenReceivers) {
        return new Channel<>(createQueue(), alternationBetweenReceivers);
    }

    public Queue createQueue(String id) {
        String queueId = idStore.compareAndSetId(id);
        Queue queue = new Queue(new QueueAnchor(queueId), core);
        core.scheduleAdminTask(new CreateQueueTask(core, queue));
        return queue;
    }

    public CompletableFuture<Void> shutdown() {
        return shutdown(10, TimeUnit.SECONDS);
    }

    public CompletableFuture<Void> shutdown(int duration, TimeUnit unit) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        core.scheduleAdminTask(new ShutdownTask(toString(), core, future, duration, unit));
        return future;
    }

    public void addDLQListener(DeadLetterListener deadLetterListener) {
        core.addDLQListener(deadLetterListener);
    }

    public boolean removeDLQListener(DeadLetterListener deadLetterListener) {
        return core.removeDLQListener(deadLetterListener);
    }

    public List<DeadLetterEntry> getDLQEntries() {
        return core.getDLQEntries();
    }

    public void clearDLQ() {
        core.clearDLQ();
    }
}
