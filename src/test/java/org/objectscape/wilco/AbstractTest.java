package org.objectscape.wilco;

/**
 * Created by plohmann on 29.04.2015.
 */
public abstract class AbstractTest {

    protected Wilco wilco = Wilco.newInstance(new Config());
    protected Queue globalQueue = wilco.createQueue();

    public void async(Runnable runnable) {
        globalQueue.execute(runnable);
    }

}
