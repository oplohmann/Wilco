package org.objectscape.wilco.core;

/**
 * marker interface which marks a class as being access by the scheduler only.
 * Therefore various internal variables need not be thread safe as the scheduler
 * thread is the only thread that reads and writes them.
 *
 * Created by plohmann on 19.02.2015.
 */
public interface SchedulerControlled {
}
