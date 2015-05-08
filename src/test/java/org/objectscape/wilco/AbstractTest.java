package org.objectscape.wilco;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.ExecutionException;

/**
 * Created by plohmann on 29.04.2015.
 */
public abstract class AbstractTest {

    protected Wilco wilco;
    protected Queue globalQueue;

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
        globalQueue.close();
        wilco.shutdown();
        // TODO - wilco.shutdown().get(); does not work yet, because shutdown is not fully iplemented
    }

}
