package org.objectscape.wilco;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.objectscape.wilco.core.tasks.util.ShutdownResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by plohmann on 29.04.2015.
 */
public abstract class AbstractTest {

    protected Wilco wilco;
    protected Queue globalQueue;
    protected boolean shutdown = true;

    public void async(Runnable runnable) {
        globalQueue.execute(runnable);
    }

    @Before
    public void startUp() {
        wilco = Wilco.newInstance(new Config());
        globalQueue = wilco.createQueue();
    }

    @After
    public void shutdown() throws ExecutionException, InterruptedException, TimeoutException {
        if(!shutdown) {
            return;
        }
        globalQueue.close();
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.shutdown();
        shutdownResponse.get(5, TimeUnit.SECONDS);
    }

}
