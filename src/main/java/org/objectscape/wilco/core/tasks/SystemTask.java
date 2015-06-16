package org.objectscape.wilco.core.tasks;

/**
 * Created by plohmann on 16.06.2015.
 */
public abstract class SystemTask extends Task {

    @Override
    public int priority() {
        return SYSTEM_PRIORITY;
    }

}
