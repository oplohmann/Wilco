package org.objectscape.wilco;

import org.objectscape.wilco.core.WilcoCore;
import org.objectscape.wilco.util.ClosedOnceGuard;
import org.objectscape.wilco.util.IdStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by plohmann on 11.06.2015.
 */
public class Wilco {

    private final static Logger LOG = LoggerFactory.getLogger(Wilco.class);

    final private IdStore idStore = new IdStore();
    final private WilcoCore core;

    final private ClosedOnceGuard shutdownGuard = new ClosedOnceGuard();

    public Wilco() {
        this(new Config());
    }

    public static Wilco newInstance(Config config) {
        return new Wilco(config);
    }

    public Wilco(Config config) {
        super();
        if(config == null) {
            throw new NullPointerException("config null");
        }

        if(true) {
            throw new RuntimeException("not yet implemented");
        }
    }

}
