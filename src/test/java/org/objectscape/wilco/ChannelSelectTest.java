package org.objectscape.wilco;


import org.junit.Test;

/**
 * Created by Nutzer on 22.05.2015.
 */
public class ChannelSelectTest extends AbstractTest {

    // @Ignore // implementation in progress
    @Test
    public void simpleSelect() throws InterruptedException {
        Channel<String> a = wilco.createChannel("a");
        Channel<Integer> b = wilco.createChannel("b");

        // Simple select because callbacks are defined before any send to a channel is done,
        // which means that no internal buffering for sent items needs to be done

        wilco.createSelect().
            onCase(a, (value -> System.out.println(value))).
            onCase(b, (value -> System.out.println(value)));

        wilco.execute(() -> a.send("abc"));
        wilco.execute(() -> b.send(127));

        Thread.sleep(100000);
    }

    @Override
    protected int shutdownTimeoutInSecs() {
        return 500000;
    }
}
