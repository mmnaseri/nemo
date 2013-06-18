package com.agileapes.nemo.events.translated;

import com.agileapes.nemo.events.TranslatedEvent;
import com.agileapes.nemo.exec.ExecutorContext;

import java.io.PrintStream;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:56 PM)
 */
public class ExecutionStartedEvent extends TranslatedEvent {

    private ExecutorContext executorContext;
    private String[] arguments;
    private PrintStream output;

    public ExecutionStartedEvent(Object source) {
        super(source);
    }

    public ExecutorContext getExecutorContext() {
        return executorContext;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public PrintStream getOutput() {
        return output;
    }

}
