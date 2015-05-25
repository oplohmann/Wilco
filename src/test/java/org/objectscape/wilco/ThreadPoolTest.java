package org.objectscape.wilco;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Nutzer on 22.05.2015.
 */
public class ThreadPoolTest {

    @Ignore // no way I can get it to throw a RejectedExecutionException
    @Test(expected = RejectedExecutionException.class)
    public void forceRejectedExecutionException() throws InterruptedException {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Runnable runnable = () ->  {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Assert.assertNotNull(e);
            }
        };

        Runnable execute = () -> {
            for (int i = 0; i < 1000000; i++) {
                executor.execute(runnable);
            }
        };

        for (int i = 0; i < 100; i++) {
            new Thread(execute).start();
        }

        Thread.sleep(100000);
    }

}
