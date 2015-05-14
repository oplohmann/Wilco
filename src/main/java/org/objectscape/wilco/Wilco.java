package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.ShutdownException;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.dlq.DeadLetterEntry;
import org.objectscape.wilco.core.dlq.DeadLetterListener;
import org.objectscape.wilco.core.tasks.CreateQueueTask;
import org.objectscape.wilco.core.tasks.ShutdownTask;
import org.objectscape.wilco.util.IdStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Wilco {

    private final static Logger LOG = LoggerFactory.getLogger(Wilco.class);

    final private IdStore idStore = new IdStore();
    final private WilcoCore core;
    private boolean running = true;

    final private Object shutdownGuard = new Object();

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
        synchronized (shutdownGuard) {
            if(!running) {
                throw new ShutdownException("Wilco instance " + this + " has been shut down");
            }
            String queueId = idStore.compareAndSetId(id);
            Queue queue = new Queue(new QueueAnchor(queueId), core);
            core.scheduleAdminTask(new CreateQueueTask(core, queue));
            return queue;
        }
    }

    public CompletableFuture<Integer> shutdown() {
        return shutdown(10, TimeUnit.SECONDS);
    }

    public CompletableFuture<Integer> shutdown(int duration, TimeUnit unit) {
        synchronized (shutdownGuard) {
            if(!running) {
                throw new ShutdownException("Wilco instance " + this + " already shut down");
            }

            CompletableFuture<Integer> future = new CompletableFuture<>();
            core.scheduleAdminTask(new ShutdownTask(toString(), core, future, duration, unit));

            running = false;
            return future;
        }
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

    public boolean isRunning() {
        synchronized (shutdownGuard) {
            return running;
        }
    }
}
