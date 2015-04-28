package org.objectscape.wilco;

import org.junit.Test;
import org.objectscape.wilco.model.Ball;

import java.util.concurrent.ExecutionException;

/**
 * Created by Nutzer on 23.04.2015.
 */
public class PingPongTest {

    private Wilco wilco = Wilco.newInstance(new Config());

    @Test
    public void pingPong() throws InterruptedException, ExecutionException {
        // modeled after this sample in Go: http://talks.golang.org/2013/advconc.slide#6

        Channel<Ball> table = wilco.createChannel();

        player("ping", table);
        player("pong", table);

        table.send(new Ball());

        Thread.sleep(2000);

        table.onReceive(ball -> {
            System.out.println("grab the ball and don't re-send it; game over");
            table.close(ball);
        }).get();
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
