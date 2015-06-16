package org.objectscape.wilco;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.ExecutionException;
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
        // TODO - not yet implemented
        /*
        for(DeadLetterEntry entry : wilco.getDLQEntries()) {
            System.out.println(entry.getStackTrace());
        }
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        CompletableFuture<ShutdownResponse> shutdownResponse = wilco.shutdown();
        shutdownResponse.get(shutdownTimeoutInSecs(), TimeUnit.SECONDS);
        */
    }

    protected int shutdownTimeoutInSecs() {
        return 5;
    }

}
