package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.WilcoCore;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nutzer on 18.05.2015.
 */
public abstract class ShutdownTask extends CoreTask {

    final protected WilcoCore core;
    final protected CompletableFuture<List<Runnable>> doneSignal;
    final protected long duration;
    final protected TimeUnit unit;
    final protected long start;

    public ShutdownTask(WilcoCore core, CompletableFuture<List<Runnable>> doneSignal, long duration, TimeUnit unit, long start) {
        this.core = core;
        this.doneSignal = doneSignal;
        this.duration = duration;
        this.unit = unit;
        this.start = start;
    }

}
