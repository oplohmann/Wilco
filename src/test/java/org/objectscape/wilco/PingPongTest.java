package org.objectscape.wilco;

import junit.framework.Assert;
import org.junit.Test;
import org.objectscape.wilco.model.Ball;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nutzer on 23.04.2015.
 */
public class PingPongTest extends AbstractTest {

    @Test
    public void trivialSample() throws InterruptedException {
        // modeled after this sample in Go: http://talks.golang.org/2013/advconc.slide#5
        AtomicInteger valueHolder = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);
        int someInt = 127;

        Channel<Integer> channel = wilco.createChannel();
        channel.onReceive(value -> {
            valueHolder.compareAndSet(0, value);
            latch.countDown();
        });

        channel.send(someInt);

        boolean noTimeOut = latch.await(4, TimeUnit.SECONDS);
        Assert.assertTrue(noTimeOut);
        Assert.assertEquals(someInt, valueHolder.get());
    }

    @Test
    public void pingPong() throws InterruptedException, ExecutionException {
        // modeled after this sample in Go: http://talks.golang.org/2013/advconc.slide#6

        Channel<Ball> table = wilco.createChannel(Alternation.Cyclic);

        player("ping", table);
        player("pong", table);

        table.send(new Ball()); // game on; toss the ball

        Thread.sleep(1000);

        table.onReceive(ball -> {
            System.out.println("grab the ball and don't re-send it; game over");
            table.close(ball);
        }).get();
    }

    @Test(expected = TimeoutException.class)
    public void pingPongPseudoDeadlockDetection() throws InterruptedException, ExecutionException, TimeoutException {
        // modeled after this sample in Go: http://talks.golang.org/2013/advconc.slide#7

        Channel<Ball> table = wilco.createChannel(Alternation.Cyclic);

        player("ping", table);
        player("pong", table);

        // Line below quoted out to create "deadlock", which is not detected at compile time as in the
        // sample in Go, but nevertheless the TimeoutException indicates that something went wrong.

        // table.send(new Ball()); // game on; toss the ball

        table.onReceive(ball -> {
            System.out.println("grab the ball and don't re-send it; game over");
            table.close(ball);
        }).get(2, TimeUnit.SECONDS);  // timeout after 1 second indicates that something went wrong
    }

    private void player(String name, Channel<Ball> table) {
        table.onReceive(ball -> {
            ball.setHits(ball.getHits() + 1);
            System.out.println(name + ": " + ball.getHits());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) { }
            table.send(ball);
        });
    }

}
