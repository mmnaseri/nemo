package com.agileapes.nemo.event.impl.events;

import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.exec.Execution;
import com.agileapes.nemo.exec.Executor;

/**
 * This event occurs whenever a call is made to {@link Executor#perform(com.agileapes.nemo.exec.Execution)}
 * to signify that an execution is being performed.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:54 PM)
 */
public class PerformingExecutionEvent extends Event {

    private final Executor executor;
    private Execution execution;

    public PerformingExecutionEvent(Executor executor, Execution execution) {
        super(executor);
        this.executor = executor;
        this.execution = execution;
    }

    public Executor getExecutor() {
        return executor;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }
}
