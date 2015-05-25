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
    protected boolean shutdown = true;

    @Before
    public void startUp() {
        wilco = Wilco.newInstance(new Config());
    }

    @After
    public void shutdown() throws ExecutionException, InterruptedException, TimeoutException {
        if(!shutdown) {
            return;
        }
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.shutdown();
        shutdownResponse.get(shutdownTimeoutInSecs(), TimeUnit.SECONDS);
    }

    protected int shutdownTimeoutInSecs() {
        return 5;
    }

}
