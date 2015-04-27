package org.objectscape.wilco;

import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.IndirectionException;

import java.lang.reflect.MalformedParametersException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Nutzer on 27.03.2015.
 */
public class ScratchTest {

    @Test
    public void scratch() throws InterruptedException {

        AtomicReference<String> ref = new AtomicReference<>();
        System.out.println(ref.compareAndSet(null, "abc"));
        System.out.println(ref.compareAndSet(null, "def"));
        System.out.println(ref.get());

    }
}
