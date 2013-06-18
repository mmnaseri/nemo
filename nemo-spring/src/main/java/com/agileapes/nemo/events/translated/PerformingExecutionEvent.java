package com.agileapes.nemo.events.translated;

import com.agileapes.nemo.events.TranslatedEvent;
import com.agileapes.nemo.exec.Execution;
import com.agileapes.nemo.exec.Executor;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 4:28 PM)
 */
public class PerformingExecutionEvent extends TranslatedEvent {

    private Executor executor;
    private Execution execution;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public PerformingExecutionEvent(Object source) {
        super(source);
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
