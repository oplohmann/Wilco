package org.objectscape.wilco.util;

/**
 * Created by plohmann on 03.03.2015.
 */
public class Node<T> {

    private T contents;
    private Node<T> next;

    public Node(T contents) {
        this.contents = contents;
    }

    public void setNext(Node<T> node) {
        next = node;
    }

    public Node<T> getNext() {
        return next;
    }

    public T getContents() {
        return contents;
    }

    public void setContents(T contents) {
        this.contents = contents;
    }
}
