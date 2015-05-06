package org.objectscape.wilco;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by Nutzer on 27.03.2015.
 */
public class ScratchTest {

    @Test
    public void scratch() throws InterruptedException {

        Thread myThread = new Thread(()-> {
            System.out.println("foo");
        });
        Thread thread = Thread.currentThread();
        // System.out.println(thread.equals(thread));
        // System.out.println(myThread.equals(thread));

        AtomicMarkableReference<Thread> atomicRef = new AtomicMarkableReference(null, false);
        boolean[] markHolder = new boolean[1];
        System.out.println(markHolder[0]);

        while(!atomicRef.isMarked()) {
            atomicRef.attemptMark(null, true);
        }

        System.out.println(atomicRef.isMarked());
        atomicRef.get(markHolder);
        System.out.println(markHolder[0]);

        Thread currentThread = Thread.currentThread();
        try {
            while(!atomicRef.compareAndSet(null, currentThread, false, false)) {
                if(atomicRef.isMarked()) {
                    throw new RuntimeException("queue closed");
                } else {
                    System.out.println("other thread won");
                }
            }
            // do my stuff inside critical section
        } finally {
            atomicRef.set(null, false); // leave critical section
        }

    }
}
