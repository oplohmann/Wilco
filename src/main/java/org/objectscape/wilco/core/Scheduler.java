package org.objectscape.wilco.core;

import org.objectscape.wilco.core.dlc.DeadLetterEntry;
import org.objectscape.wilco.core.dlc.DeadLetterQueue;
import org.objectscape.wilco.core.tasks.CoreTask;
import org.objectscape.wilco.util.TransferPriorityQueue;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TransferQueue;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Scheduler implements Runnable {

    final private Context context;
    final private TransferPriorityQueue<CoreTask> entryQueue;
    final private ThreadPoolExecutor executor;

    private boolean proceed = true;

    public Scheduler(TransferPriorityQueue<CoreTask> entryQueue, ThreadPoolExecutor executor, DeadLetterQueue deadLetterQueue) {
        this.entryQueue = entryQueue;
        this.executor = executor;
        this.context = new Context(executor, entryQueue, deadLetterQueue);
    }

    @Override
    public void run()
    {
        while(proceed)
        {
            CoreTask coreTask = null;

            try {
                coreTask = entryQueue.take();
                proceed = coreTask.run(context);
            }
            catch (Throwable e) {
                context.addToDeadLetterQueue(coreTask.queueId(), e);
            }
        }
    }

}
