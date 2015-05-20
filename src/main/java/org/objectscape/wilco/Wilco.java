package org.objectscape.wilco;

import org.objectscape.wilco.core.*;
import org.objectscape.wilco.core.dlq.DeadLetterEntry;
import org.objectscape.wilco.core.dlq.DeadLetterListener;
import org.objectscape.wilco.core.tasks.CreateQueueTask;
import org.objectscape.wilco.core.tasks.PrepareShutdownTask;
import org.objectscape.wilco.util.ClosedOnceGuard;
import org.objectscape.wilco.util.IdStore;
import org.objectscape.wilco.util.QueueAnchorPair;
import org.objectscape.wilco.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Wilco {

    private final static Logger LOG = LoggerFactory.getLogger(Wilco.class);

    final private IdStore idStore = new IdStore();
    final private WilcoCore core;

    final private ClosedOnceGuard shutdownGuard = new ClosedOnceGuard();

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

        Ref<Queue> queueRef = new Ref<>();

        boolean guardOpen = shutdownGuard.runIfOpen(()-> {
            String queueId = idStore.compareAndSetId(id);
            QueueAnchor queueAnchor = new QueueAnchor(queueId);
            Queue queue = new Queue(queueAnchor, core);
            core.scheduleAdminTask(new CreateQueueTask(core, new QueueAnchorPair(queue, queueAnchor)));
            queueRef.set(queue);
        });

        if(!guardOpen) {
            throw new ShutdownException("Wilco instance " + this + " has been shut down");
        }

        return queueRef.get();
    }

    public CompletableFuture<ShutdownResponse> shutdown() {
        return shutdown(10, TimeUnit.SECONDS);
    }

    public CompletableFuture<ShutdownResponse> shutdown(long duration, TimeUnit unit) {
        if(unit == null) {
            throw new NullPointerException("unit null");
        }

        CompletableFuture<ShutdownResponse> future = new CompletableFuture<>();

        boolean guardWasOpen = shutdownGuard.closeAndRun(()->
            core.scheduleAdminTask(new PrepareShutdownTask(toString(), core, future, duration, unit))
        );

        if(!guardWasOpen) {
            throw new ShutdownException("Wilco instance " + this + " already shut down");
        }

        return future;
    }

    public void tryShutdown(long duration, TimeUnit unit, Consumer<ShutdownCallback> callback) {
        if(unit == null) {
            throw new NullPointerException("unit null");
        }
        if(callback == null) {
            throw new NullPointerException("callback null");
        }


    }

    public boolean isSchedulerRunning() {
        return core.isSchedulerRunning();
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

    public boolean isShutdown() {
        return shutdownGuard.isClosed();
    }

}
