package org.objectscape.wilco;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectscape.wilco.util.ClosedOnceForeverGuard;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by plohmann on 13.05.2015.
 */
public class ClosedOnceForeverGuardTest {

    private ClosedOnceForeverGuard guard;

    @Test
    public void close() {
        Assert.assertTrue(guard.isOpen());
        boolean success = guard.close();
        Assert.assertTrue(success);
        Assert.assertTrue(guard.isClosed());
    }

    @Test
    public void closeAndRun() {
        Assert.assertTrue(guard.isOpen());
        AtomicBoolean wasRun = new AtomicBoolean(false);
        boolean success = guard.closeAndRun(() -> wasRun.compareAndSet(false, true));
        Assert.assertTrue(success);
        Assert.assertTrue(wasRun.get());
        Assert.assertTrue(guard.isClosed());
    }

    @Test
    public void runIfOpen() {
        Assert.assertTrue(guard.isOpen());
        AtomicBoolean wasRunOpen = new AtomicBoolean(false);
        boolean success = guard.runIfOpen(() -> wasRunOpen.compareAndSet(false, true));
        Assert.assertTrue(success);
        Assert.assertTrue(wasRunOpen.get());
        Assert.assertTrue(guard.isOpen());

        guard.close();
        Assert.assertTrue(guard.isClosed());
        AtomicBoolean wasRunClose = new AtomicBoolean(false);
        success = guard.runIfOpen(() -> wasRunClose.compareAndSet(false, true));
        Assert.assertFalse(wasRunClose.get()); // wasRunClose did not run
        Assert.assertFalse(success);
    }

    @Test
    public void run() {
        Assert.assertTrue(guard.isOpen());
        final AtomicBoolean wasRunOpen = new AtomicBoolean(false);
        boolean success = guard.run(ClosedOnceForeverGuard.Mark.OPEN, () -> wasRunOpen.compareAndSet(false, true));
        Assert.assertTrue(success);
        Assert.assertTrue(wasRunOpen.get());
        Assert.assertTrue(guard.isOpen());

        guard.close();

        final AtomicBoolean wasRunClose = new AtomicBoolean(false);
        success = guard.run(ClosedOnceForeverGuard.Mark.CLOSED, () -> wasRunClose.compareAndSet(false, true));
        Assert.assertTrue(success);
        Assert.assertTrue(wasRunClose.get());
        Assert.assertTrue(guard.isClosed());
    }

    @Before
    public void startUp() {
        guard = new ClosedOnceForeverGuard();
    }

    @After
    public void shutdownUp() {
        guard = null;
    }

}
