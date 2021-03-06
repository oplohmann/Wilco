package org.objectscape.wilco;


import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nutzer on 22.05.2015.
 */
public class ChannelSelectTest extends AbstractTest {

    @Test
    public void simpleSelect() throws InterruptedException {
        Channel<String> a = wilco.createChannel("a");
        Channel<Integer> b = wilco.createChannel("b");

        CountDownLatch latch = new CountDownLatch(2);

        String valueChannelA = "abc";
        Integer valueChannelB = 127;

        // Simple select because callbacks are defined before any send to a channel is done,
        // which means that no internal buffering for sent items needs to be done

        wilco.createSelect().
            onCase(a, (value -> {
                Assert.assertEquals(valueChannelA, value);
                latch.countDown();
            })).
            onCase(b, (value -> {
                Assert.assertEquals(valueChannelB, value);
                latch.countDown();
            }));

        wilco.execute(() -> a.send(valueChannelA));
        wilco.execute(() -> b.send(valueChannelB));

        boolean noTimeOut = latch.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(noTimeOut);
    }

    @Test
    public void deferredSelect() throws InterruptedException {
        Channel<String> channelA = wilco.createChannel("a");
        Channel<Integer> channelB = wilco.createChannel("b");

        String valueChannelA = "abc";
        Integer valueChannelB = 127;

        // Contrary to ChannelSelectTest.simpleSelect values are send to the channels before
        // ChannelSelect callbacks were defined.
        wilco.execute(() -> channelA.send(valueChannelA));
        wilco.execute(() -> channelB.send(valueChannelB));

        CountDownLatch latch = new CountDownLatch(2);

        // Simple select because callbacks are defined before any send to a channel is done,
        // which means that no internal buffering for sent items needs to be done

        wilco.createSelect().
                onCase(channelA, (value -> {
                    Assert.assertEquals(valueChannelA, value);
                    latch.countDown();
                })).
                onCase(channelB, (value -> {
                    Assert.assertEquals(valueChannelB, value);
                    latch.countDown();
                }));

        boolean noTimeOut = latch.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(noTimeOut);
    }

    @Test
    public void onTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        long timeoutPeriod = 2L;

        ChannelSelect select = wilco.createSelect().onTimeout(timeoutPeriod, TimeUnit.SECONDS, ()-> latch.countDown());

        boolean noTimeoutOccured = latch.await(timeoutPeriod * 2, TimeUnit.SECONDS);
        Assert.assertTrue(noTimeoutOccured);

        select.clearTimeout();
    }

    @Override
    protected int shutdownTimeoutInSecs() {
        return 5;
    }
}
