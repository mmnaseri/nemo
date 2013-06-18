package com.agileapes.nemo.events.translated;

import com.agileapes.nemo.events.TranslatedEvent;
import com.agileapes.nemo.exec.ExecutorContext;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 4:26 PM)
 */
public class ExecutionErrorEvent extends TranslatedEvent {

    private ExecutorContext executorContext;
    private Exception error;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public ExecutionErrorEvent(Object source) {
        super(source);
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
