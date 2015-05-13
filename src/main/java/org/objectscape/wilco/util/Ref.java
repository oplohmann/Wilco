package org.objectscape.wilco.util;

/**
 * Created by plohmann on 13.05.2015.
 */
public class Ref<T> {

    private T ref;

    public Ref() {
    }

    public Ref(T ref) {
        this.ref = ref;
    }

    public T get() {
        return ref;
    }

    public void set(T ref) {
        this.ref = ref;
    }
}
