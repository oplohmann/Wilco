package org.objectscape.wilco;

import org.junit.Test;

/**
 * Created by Nutzer on 27.03.2015.
 */
public class ScratchTest {

    @Test
    public void scratch() throws InterruptedException {

        Alternation alternation = Alternation.Random;
        System.out.println(Alternation.Cyclic.equals(alternation));

    }
}
