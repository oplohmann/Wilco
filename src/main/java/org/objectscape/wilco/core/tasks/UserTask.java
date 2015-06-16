package org.objectscape.wilco.core.tasks;

/**
 * Created by plohmann on 16.06.2015.
 */
public abstract class UserTask extends Task {

    @Override
    public int priority() {
        return USER_PRIORITY;
    }

}
