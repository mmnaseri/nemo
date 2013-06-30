package com.agileapes.nemo.event.impl.events;

import com.agileapes.couteau.context.contract.Event;
import com.agileapes.nemo.exec.ExecutorContext;

/**
 * This event will occur once there has been an error while executing the application through the
 * {@link ExecutorContext}
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:49 PM)
 */
public class ExecutionErrorEvent extends Event {

    private final ExecutorContext executorContext;
    private Exception error;

    public ExecutionErrorEvent(ExecutorContext executorContext, Exception error) {
        super(executorContext);
        this.executorContext = executorContext;
        this.error = error;
    }

    public ExecutorContext getExecutorContext() {
        return executorContext;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

}
