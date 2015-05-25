package org.objectscape.wilco.core.tasks;

import org.objectscape.wilco.core.AsyncQueueAnchor;
import org.objectscape.wilco.core.Context;

/**
 * Created by Nutzer on 25.05.2015.
 */
public class CompletedAsyncTask extends CoreTask {

    private AsyncQueueAnchor queueAnchor;
    private Integer taskId;

    public CompletedAsyncTask(AsyncQueueAnchor queueAnchor, Integer taskId) {
        this.queueAnchor = queueAnchor;
        this.taskId = taskId;
    }

    @Override
    public boolean run(Context context) {
        queueAnchor.removeTask(taskId);
        clear();
        return true;
    }

    @Override
    public int priority() {
        return MEDIUM_PRIORITY;
    }

    protected void clear() {
        queueAnchor = null;
    }
}
