package org.objectscape.wilco;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Assert;
import org.junit.Test;
import org.objectscape.wilco.util.LinkedQueue;

import java.util.LinkedList;

/**
 * Created by plohmann on 03.03.2015.
 */
public class LinkedQueueTest {

    @Test
    public void addRemoveOneElement() {
        LinkedQueue<Integer> list = new LinkedQueue<>();
        list.addLast(1);
        Assert.assertEquals(1, list.size());
        int value = list.peekFirst();
        Assert.assertEquals(1, 1);
        Assert.assertEquals(1, list.size());
        value = list.removeFirst();
        Assert.assertEquals(1, 1);
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void addRemoveMultipleElements() {
        LinkedQueue<Integer> list = new LinkedQueue<>();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        Assert.assertEquals(3, list.size());
        int value = list.peekFirst();
        Assert.assertEquals(1, value);
        Assert.assertEquals(3, list.size());

        value = list.removeFirst();
        Assert.assertEquals(1, value);
        Assert.assertEquals(2, list.size());

        value = list.removeFirst();
        Assert.assertEquals(2, value);
        Assert.assertEquals(1, list.size());

        value = list.removeFirst();
        Assert.assertEquals(3, value);
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void addAndPoll() {
        LinkedQueue<Object> list = new LinkedQueue<>();
        Object object = new Object();
        list.addLast(object);
        Assert.assertEquals(1, list.size());
        Object value = list.pollFirst();
        Assert.assertEquals(object, value);
    }

    @Test
    public void addTwoElementsAndPoll() {
        LinkedQueue<Object> list = new LinkedQueue<>();
        Object object1 = new Object();
        list.addLast(object1);
        Object object2 = new Object();
        list.addLast(object2);
        Assert.assertEquals(2, list.size());
        Object value = list.pollFirst();
        Assert.assertEquals(object1, value);
    }

    @Test
    public void clear() {
        LinkedQueue<Integer> list = new LinkedQueue<>();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        list.clear();
    }

    @Test
    public void addRemove() {
        LinkedQueue<Integer> list = new LinkedQueue<Integer>();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        list.addLast(4);
        Assert.assertEquals(list.peekFirst().intValue(), 1);
        Assert.assertEquals(list.removeFirst().intValue(), 1);

        Assert.assertEquals(list.peekFirst().intValue(), 2);
        Assert.assertEquals(list.removeFirst().intValue(), 2);

        Assert.assertEquals(list.peekFirst().intValue(), 3);
        Assert.assertEquals(list.removeFirst().intValue(), 3);

        Assert.assertEquals(list.peekFirst().intValue(), 4);
        Assert.assertEquals(list.removeFirst().intValue(), 4);
    }

    @Test
    @Ignore
    public void littlePerformanceEstimation() {

        // for n > 7.000.000 LinkedList causes OutOfMemoryError
        // while LinkedQueue doesn't. LinkedTransferQueue causes no
        // OutOfMemoryError, but is much slower than LinkedQueue
        long n = 3000000;
        long start = System.currentTimeMillis();
        LinkedQueue<Integer> list = new LinkedQueue<Integer>();
        for (int i = 0; i < n; i++) {
            list.addLast(i);
        }

        while(!list.isEmpty()) {
            list.removeFirst();
        }

        // System.out.println(System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        LinkedList<Integer> list2 = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            list2.add(i);
        }

        while(!list2.isEmpty()) {
            list2.remove(0);
        }

        // System.out.println(System.currentTimeMillis() - start);
    }
}
