package org.objectscape.wilco;

import org.objectscape.wilco.core.QueueAnchor;
import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.tasks.ResumeChannelTask;
import org.objectscape.wilco.core.tasks.ScheduledTask;
import org.objectscape.wilco.core.tasks.SuspendChannelTask;

/**
 * Created by plohmann on 19.02.2015.
 */
public class Queue {

    final private QueueAnchor queueAnchor;
    final private WilcoCore core;

    public Queue(QueueAnchor queueAnchor, WilcoCore core) {
        this.queueAnchor = queueAnchor;
        this.core = core;
    }

    public String getId() {
        return queueAnchor.getId();
    }

    public int size() {
        return queueAnchor.size();
    }

    public void execute(Runnable runnable) {
        queueAnchor.checkClosed();
        core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable));
    }

    public void execute(Runnable runnable, Runnable whenDoneRunnable) {
        queueAnchor.checkClosed();
        core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable, whenDoneRunnable));
    }

    protected void executeIgnoreClose(Runnable runnable) {
        // does not check whether closed.
        core.scheduleUserTask(new ScheduledTask(queueAnchor, runnable));
    }

    public void suspend() {
        queueAnchor.checkClosed();
        core.scheduleAdminTask(new SuspendChannelTask(queueAnchor));
    }

    public void suspend(Runnable whenDoneRunnable) {
        queueAnchor.checkClosed();
        core.scheduleAdminTask(new SuspendChannelTask(queueAnchor, whenDoneRunnable));
    }

    public void resume() {
        queueAnchor.checkClosed();
        core.scheduleAdminTask(new ResumeChannelTask(queueAnchor));
    }

    public void resume(Runnable whenDoneRunnable) {
        queueAnchor.checkClosed();
        core.scheduleAdminTask(new ResumeChannelTask(queueAnchor, whenDoneRunnable));
    }

    public void close() {
        queueAnchor.close();
    }

}
