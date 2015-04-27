package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;

/**
 *
 * Using a CoreTask over a Runnable since the JVM can more efficiently invoke
 * methods of an abstract class than a interface.
 *
 * Created by plohmann on 19.02.2015.
 */
public abstract class CoreTask {

    public static final int MAX_PRIORITY = 0;
    public static final int MEDIUM_PRIORITY = 1;
    public static final int MIN_PRIORITY = 2;

    public abstract boolean run(Context context);

    public abstract int priority();

    public String queueId() {
        return null;
    }

}
