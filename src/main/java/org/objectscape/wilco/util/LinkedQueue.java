package org.objectscape.wilco.util;

import java.util.NoSuchElementException;

/**
 * Created by plohmann on 03.03.2015.
 */
public class LinkedQueue<T> {

    private Node<T> head = null;
    private Node<T> tail = null;
    private int size = 0;

    public void addLast(T t) {
        Node<T> newNode = new Node<T>(t);
        if(size == 0) {
            head = newNode;
        }
        else if(size == 1) {
            tail = newNode;
            head.setNext(tail);
        }
        else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    public T peekFirst() {
        if(head == null) {
            return null;
        }
        return head.getContents();
    }

    public T pollFirst() {
        if(head == null) {
            return null;
        }
        return unlinkFirst();
    }

    public T removeFirst() {
        if(head == null) {
            throw new NoSuchElementException();
        }
        return unlinkFirst();
    }


    private T unlinkFirst() {
        assert head != null;
        T contents = head.getContents();
        Node<T> next = head.getNext();
        head.setNext(null); // make sure the current head becomes collected by the GC
        head = next;
        size--;
        if(size == 0) {
            head = null;
            tail = null;
        }
        return contents;
    }

    /**
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */
    public void clear() {
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        Node<T> node = head;
        while (node != null) {
            Node<T> next = node.getNext();
            node.setNext(null);
            node.setContents(null);
            node = next;
        }
        clearRaw();
    }


    public void clearRaw() {
        // Only clear inst vars. Uses less CPU time than <code>clear()</code>.
        // To clear all of the links between nodes to help the GC call <code>clear()</code>
        head = tail = null;
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

}
