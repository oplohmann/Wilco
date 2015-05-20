package org.objectscape.wilco.core;

import org.objectscape.wilco.core.dlq.DeadLetterQueue;
import org.objectscape.wilco.core.tasks.CoreTask;
import org.objectscape.wilco.util.TransferPriorityQueue;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Scheduler implements Runnable {

    final private Context context;
    final private TransferPriorityQueue<CoreTask> entryQueue;
    final private AtomicBoolean running = new AtomicBoolean(false);

    private boolean proceed = true;

    public Scheduler(TransferPriorityQueue<CoreTask> entryQueue, ThreadPoolExecutor executor, DeadLetterQueue deadLetterQueue) {
        this.entryQueue = entryQueue;
        this.context = new Context(executor, entryQueue, deadLetterQueue);
    }

    @Override
    public void run()
    {
        running.compareAndSet(false, true);
        while(proceed)
        {
            CoreTask coreTask = null;

            try {
                coreTask = entryQueue.take();
                proceed = coreTask.run(context);
            }
            catch (Throwable e) {
                context.addToDeadLetterQueue(coreTask.queueId().get(), e);
            }
        }
        running.compareAndSet(true, false);
    }

    public boolean isRunning() {
        return running.get();
    }

}
