package org.objectscape.wilco;

import java.util.function.Consumer;

/**
 * Created by Nutzer on 22.05.2015.
 */
public class Select {

    public <T> Select onCase(Channel<T> channel, Consumer<T> runnable) {
        return this;
    }
}
