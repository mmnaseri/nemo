package com.agileapes.nemo.event.impl.events;

import com.agileapes.couteau.context.contract.Event;
import com.agileapes.nemo.exec.ExecutorContext;

import java.io.PrintStream;

/**
 * This event occurs right before an execution is being run by the application.
 * Notice that this happens through {@link ExecutorContext#execute(java.io.PrintStream, String...)}
 * and not {@link com.agileapes.nemo.exec.Executor#execute(java.io.PrintStream, String...)}
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/16/13, 3:26 PM)
 */
public class ExecutionStartedEvent extends Event {

    private final ExecutorContext executorContext;
    private String[] arguments;
    private PrintStream output;

    public ExecutionStartedEvent(ExecutorContext source, String[] arguments, PrintStream output) {
        super(source);
        this.executorContext = source;
        this.arguments = arguments;
        this.output = output;
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
