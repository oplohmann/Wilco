package org.objectscape.wilco;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by plohmann on 28.05.2015.
 */
public class WilcoTest extends AbstractTest {

    @Test
    public void onIdle() {
        wilco.onIdleAfter(10L, TimeUnit.SECONDS, ()-> {});
        System.out.println("WilcoTest.onIdle not yet implemented");
    }
}
