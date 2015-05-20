package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.core.tasks.util.ShutdownResponse;
import org.objectscape.wilco.core.tasks.util.ShutdownTaskInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nutzer on 18.05.2015.
 */
public abstract class ShutdownTask extends CoreTask {

    final protected ShutdownTaskInfo shutdownTaskInfo;

    public ShutdownTask(ShutdownTaskInfo shutdownTaskInfo) {
        this.shutdownTaskInfo = shutdownTaskInfo;
    }

    public WilcoCore getCore() {
        return shutdownTaskInfo.getCore();
    }

    public TimeUnit getUnit() {
        return shutdownTaskInfo.getUnit();
    }

    public long getStart() {
        return shutdownTaskInfo.getStart();
    }

    public long getDuration() {
        return shutdownTaskInfo.getDuration();
    }

    public CompletableFuture<ShutdownResponse> getDoneSignal() {
        return shutdownTaskInfo.getDoneSignal();
    }

    protected long getDurationInMillis() {
        return getUnit().toMillis(getDuration());
    }
}
