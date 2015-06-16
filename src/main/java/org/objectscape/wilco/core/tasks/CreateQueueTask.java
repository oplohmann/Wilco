package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.Context;
import org.objectscape.wilco.core.QueueCore;

/**
 * Created by plohmann on 16.06.2015.
 */
public class CreateQueueTask extends SystemTask {

    private QueueCore queueCore;

    public CreateQueueTask(QueueCore queueCore) {
        this.queueCore = queueCore;
    }

    @Override
    public boolean run(Context context) {
        context.getCore().addQueue(queueCore);
        return true;
    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void clear() {
        queueCore = null;
    }
}
