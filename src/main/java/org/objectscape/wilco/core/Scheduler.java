package org.objectscape.wilco.core;

import org.objectscape.wilco.core.tasks.Task;
import org.objectscape.wilco.util.TransferPriorityQueue;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Scheduler implements Runnable {

    final private Context context;
    final private TransferPriorityQueue<Task> schedulerQueue;
    final private AtomicBoolean running = new AtomicBoolean(false);

    private AtomicLong lastTimeActive = new AtomicLong();
    private boolean proceed = true;

    public Scheduler(WilcoCore core, TransferPriorityQueue<Task> schedulerQueue, ThreadPoolExecutor executor) {
        this.schedulerQueue = schedulerQueue;
        this.context = new Context(core, schedulerQueue, executor);
    }

    @Override
    public void run()
    {
        running.compareAndSet(false, true);
        lastTimeActive.getAndSet(System.currentTimeMillis());

        while(proceed)
        {
            Task task = null;

            try {
                task = schedulerQueue.take();
                proceed = task.run(context);
                task.clear();
            }
            catch (Exception e) {
                // TODO - NYI
                // context.addToDeadLetterQueue(task.queueId().get(), e);
                if(task != null) {
                    task.onException(e);
                }
            }
        }

        running.compareAndSet(true, false);
    }

    public boolean isRunning() {
        return running.get();
    }

    public long getLastTimeActive() {
        return lastTimeActive.get();
    }
}
