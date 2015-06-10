package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.WilcoCore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Nutzer on 04.06.2015.
 */
public class CloseChannelTask<T> extends CloseQueueTask {

    final private T closeValue;
    final private CompletableFuture<T> closedFuture;
    final private AtomicReference<Runnable> onCloseRef;

    public CloseChannelTask(WilcoCore core, String queueId, T closeValue, CompletableFuture<T> closedFuture, AtomicReference<Runnable> onCloseRef) {
        super(core, queueId);
        this.closeValue = closeValue;
        this.closedFuture = closedFuture;
        this.onCloseRef = onCloseRef;
    }

    @Override
    public boolean run(Context context) {
        super.run(context);
        closedFuture.complete(closeValue);
        if (onCloseRef.get() != null) {
            context.getExecutor().execute(() -> onCloseRef.get().run());
        }
        return true;
    }
}
