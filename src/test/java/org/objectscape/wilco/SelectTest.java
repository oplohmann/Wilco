package org.objectscape.wilco;

import jdk.nashorn.internal.ir.annotations.Ignore;

/**
 * Created by Nutzer on 22.05.2015.
 */
public class SelectTest extends AbstractTest {

    @Ignore // implementation in progress
    // @Test
    public void simpleSelect() throws InterruptedException {
        Channel<String> a = wilco.createChannel("a");
        Channel<Integer> b = wilco.createChannel("b");

        wilco.execute(() -> a.send("a"));
        wilco.execute(() -> b.send(127));

        Thread.sleep(1000);

        a.onReceive(str -> System.out.println(str));

        wilco.createSelect().
            onCase(a, (value -> System.out.println(value))).
            onCase(b, (value -> System.out.println(value)));

        Thread.sleep(1000);
    }

}
