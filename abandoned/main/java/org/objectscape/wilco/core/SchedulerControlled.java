package org.objectscape.wilco.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as only thread-safe when called from the single Scheduler. The single
 * Scheduler executes one task after the other. In that way no other thread can at the same
 * time access the same variable resulting in the access as thread-safe as long the implementor
 * of the method marked as SchedulerControlled never calls this method from outside the Scheduler.
 *
 * Created by plohmann on 19.02.2015.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface SchedulerControlled {
}
