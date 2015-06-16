package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueCore;
import org.objectscape.wilco.core.Scheduler;
import org.objectscape.wilco.core.ShutdownException;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.tasks.CreateQueueTask;
import org.objectscape.wilco.util.ClosedOnceGuard;
import org.objectscape.wilco.util.IdStore;
import org.objectscape.wilco.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by plohmann on 11.06.2015.
 */
public class Wilco {

    private final static Logger LOG = LoggerFactory.getLogger(Wilco.class);

    final private IdStore idStore = new IdStore();
    final private WilcoCore core;

    final private ClosedOnceGuard shutdownGuard = new ClosedOnceGuard();

    public static Wilco newInstance() {
        return new Wilco(new Config());
    }

    public static Wilco newInstance(Config config) {
        return new Wilco(config);
    }

    private Wilco(Config config) {
        super();
        if(config == null) {
            throw new NullPointerException("config null");
        }
        core = new WilcoCore(config);
    }

    public Queue createQueue() {
        return createQueue(idStore.generateId(), false);
    }

    public Queue createQueue(String queueId) {
        if(queueId == null) {
            throw new NullPointerException("queueId null");
        }
        return createQueue(queueId, true);
    }

    private Queue createQueue(String id, boolean verifyIdUnique) {

        if(id == null) {
            throw new NullPointerException("id null");
        }

        Ref<Queue> queueRef = new Ref<>();

        boolean guardOpen = shutdownGuard.runIfOpen(()-> {
            String queueId = verifyIdUnique ? idStore.compareAndSetId(id) : id;
            Scheduler scheduler = core.getLeastLoadedScheduler();
            QueueCore queueCore = new QueueCore(queueId, scheduler.getId());
            Queue queue = new Queue(queueCore);
            scheduler.scheduleSystemTask(new CreateQueueTask(queueCore));
            queueRef.set(queue);
        });

        if(!guardOpen) {
            throw new ShutdownException("Wilco instance " + this + " has been shut down");
        }

        return queueRef.get();
    }
}
