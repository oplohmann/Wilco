package org.objectscape.wilco;

import org.junit.Test;

import java.util.Optional;

/**
 * Created by Nutzer on 27.03.2015.
 */
public class ScratchTest {

    @Test
    public void scratch() {
        Optional<String> str = Optional.of("abc");
        System.out.println(str.isPresent());
    }

}
