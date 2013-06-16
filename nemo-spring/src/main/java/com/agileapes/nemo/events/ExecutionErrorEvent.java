package com.agileapes.nemo.events;

import com.agileapes.nemo.exec.ExecutorContext;
import org.springframework.context.ApplicationEvent;

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
    public ExecutionErrorEvent(SpringEventTranslator source) {
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

    public void setExecutorContext(ExecutorContext executorContext) {
        this.executorContext = executorContext;
    }

}
