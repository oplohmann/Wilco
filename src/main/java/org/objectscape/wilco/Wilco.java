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
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Wilco {

    private final static Logger LOG = LoggerFactory.getLogger(Wilco.class);

    private final static boolean WILCO_RUNNING_MARK = false;
    private final static boolean WILCO_SHUTDOWN_MARK = true;

    final private IdStore idStore = new IdStore();
    final private WilcoCore core;

    final private AtomicMarkableReference<Thread> shutdownGuard = new AtomicMarkableReference(null, WILCO_RUNNING_MARK);

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
        try {
            lockShutdown(WILCO_RUNNING_MARK);
            String queueId = idStore.compareAndSetId(id);
            Queue queue = new Queue(new QueueAnchor(queueId), core);
            core.scheduleAdminTask(new CreateQueueTask(core, queue));
            return queue;
        }
        finally {
            unlockShutdown();
        }
    }

    public CompletableFuture<Integer> shutdown() {
        return shutdown(10, TimeUnit.SECONDS);
    }

    public CompletableFuture<Integer> shutdown(int duration, TimeUnit unit) {

        boolean wasAlreadyShutdown = true;

        while(!shutdownGuard.isMarked()) {
            wasAlreadyShutdown =! shutdownGuard.attemptMark(null, WILCO_SHUTDOWN_MARK);
        }

        if(wasAlreadyShutdown) {
            throw new ShutdownException("Wilco instance " + this + " already shut down");
        }

        try {
            lockShutdown(WILCO_SHUTDOWN_MARK);
            CompletableFuture<Integer> future = new CompletableFuture<>();
            core.scheduleAdminTask(new ShutdownTask(toString(), core, future, duration, unit));
            return future;
        }
        finally {
            unlockShutdown();
        }
    }

    private void lockShutdown(boolean mark) {
        Thread currentThread = Thread.currentThread();
        while(!shutdownGuard.compareAndSet(null, currentThread, mark, mark)) {
            if(shutdownGuard.isMarked()) {
                throw new ShutdownException("Wilco instance " + this + " has been shut down");
            } else {
                LOG.debug("other thread won in lockShutdown");
            }
        }
    }

    private void unlockShutdown() {
        // leave critical section
        if(shutdownGuard.isMarked()) {
            shutdownGuard.set(null, WILCO_SHUTDOWN_MARK);
        } else {
            shutdownGuard.set(null, WILCO_RUNNING_MARK);
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
}
