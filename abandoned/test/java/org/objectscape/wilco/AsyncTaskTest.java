package org.objectscape.wilco;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Nutzer on 25.05.2015.
 */
public class AsyncTaskTest extends AbstractTest {

    @Test
    public void execute() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        wilco.execute(()-> {
            executed.compareAndSet(false, true);
            latch.countDown();
        });
        boolean noTimeout = latch.await(1, TimeUnit.SECONDS);
        Assert.assertTrue(noTimeout);
    }

}
