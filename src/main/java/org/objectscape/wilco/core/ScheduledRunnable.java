package org.objectscape.wilco.core;

/**
 * Created by plohmann on 25.03.2015.
 */
public class ScheduledRunnable {

    final private Runnable runnable;
    final private Runnable whenDoneRunnable;

    public ScheduledRunnable(Runnable runnable) {
        this.runnable = runnable;
        this.whenDoneRunnable = null;
    }

    public ScheduledRunnable(Runnable runnable, Runnable whenDoneRunnable) {
        this.runnable = runnable;
        this.whenDoneRunnable = whenDoneRunnable;
    }

    public void run() {
        runnable.run();
        if(whenDoneRunnable != null) {
            whenDoneRunnable.run();
        }
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
