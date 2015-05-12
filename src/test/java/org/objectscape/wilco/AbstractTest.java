package org.objectscape.wilco;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.concurrent.ExecutionException;

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
    public void shutdown() throws ExecutionException, InterruptedException {
        if(!shutdown) {
            return;
        }
        globalQueue.close();
        Assert.assertTrue(wilco.getDLQEntries().isEmpty());
        wilco.shutdown();
        // TODO - wilco.prepareShutdown().get(); does not work yet, because prepareShutdown is not fully iplemented
    }

}
