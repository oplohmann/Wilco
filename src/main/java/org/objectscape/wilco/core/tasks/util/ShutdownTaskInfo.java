package org.objectscape.wilco.core.tasks.util;

import org.objectscape.wilco.core.WilcoCore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nutzer on 20.05.2015.
 */
public class ShutdownTaskInfo {

    final protected WilcoCore core;
    final protected CompletableFuture<ShutdownResponse> doneSignal;
    final protected long duration;
    final protected TimeUnit unit;
    final protected long start;

    public ShutdownTaskInfo(WilcoCore core, CompletableFuture<ShutdownResponse> doneSignal, long duration, TimeUnit unit, long start) {
        this.core = core;
        this.doneSignal = doneSignal;
        this.duration = duration;
        this.unit = unit;
        this.start = start;
    }

    public WilcoCore getCore() {
        return core;
    }

    public CompletableFuture<ShutdownResponse> getDoneSignal() {
        return doneSignal;
    }

    public long getDuration() {
        return duration;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public long getStart() {
        return start;
    }
}
