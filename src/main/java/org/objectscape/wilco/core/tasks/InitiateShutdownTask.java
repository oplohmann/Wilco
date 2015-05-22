package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.tasks.util.ShutdownTaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by plohmann on 19.02.2015.
 */
public class InitiateShutdownTask extends ShutdownTask {

    final private static Logger LOG = LoggerFactory.getLogger(InitiateShutdownTask.class);

    final private String wilco;

    public InitiateShutdownTask(String wilco, ShutdownTaskInfo shutdownTaskInfo) {
        super(shutdownTaskInfo);
        this.wilco = wilco;
    }

    @Override
    public boolean run(Context context) {
        getCore().scheduleAdminTask(getRunEmptyShutdownTask());
        return true;
    }

    protected RunEmptyShutdownTask getRunEmptyShutdownTask() {
        return new RunEmptyShutdownTask(shutdownTaskInfo, getCore().closeAllQueues());
    }

    @Override
    public int priority() {
        // MAX_PRIORITY because it is important to close down all queues asap for the shutdown process to be swift
        return MAX_PRIORITY;
    }
}
