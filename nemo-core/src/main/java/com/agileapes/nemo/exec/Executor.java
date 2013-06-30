package com.agileapes.nemo.exec;

import com.agileapes.couteau.context.error.NoSuchItemException;
import com.agileapes.nemo.action.impl.ActionContext;
import com.agileapes.nemo.action.impl.SmartAction;
import com.agileapes.nemo.error.FatalExecutionException;
import com.agileapes.nemo.error.TargetNotFoundException;
import com.agileapes.nemo.event.impl.events.PerformingExecutionEvent;
import com.agileapes.nemo.option.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintStream;
import java.util.Map;

/**
 * The executor will carry out the gluing piece of work by handling input arguments and handing the task
 * over to the responsible authority classes whenever required.
 *
 * The currently in-progress execution can be determined via {@link #getExecution()}.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 19:50)
 */
public class Executor {

    private static final Log log = LogFactory.getLog(Executor.class);
    private final ActionContext actionRegistry;
    private final ExecutorContext executorContext;
    private Execution execution;
    private PrintStream output;

    Executor(ActionContext actionRegistry, ExecutorContext executorContext) {
        this.actionRegistry = actionRegistry;
        this.executorContext = executorContext;
    }

    public Execution getExecution() {
        return execution;
    }

    public PrintStream getOutput() {
        return output;
    }

    public void execute(PrintStream output, String... args) throws Exception {
        this.output = output;
        execution = new Execution(actionRegistry, args);
        perform(execution);
    }

    public void perform(Execution execution) throws Exception {
        log.debug("Performing execution: " + execution);
        execution = executorContext.publishEvent(new PerformingExecutionEvent(this, execution)).getExecution();
        final SmartAction action;
        try {
            action = (SmartAction) actionRegistry.get(execution.getTarget());
        } catch (NoSuchItemException e) {
            log.error("Specified action not found: " + execution.getTarget());
            throw new TargetNotFoundException(execution.getTarget());
        }
        if (action.isInternal()) {
            log.error("Attempting to call internal action " + execution.getTarget() + " from the command line");
            throw new IllegalAccessException("Internal action '" + execution.getTarget() + "' cannot be called from the command line");
        }
        action.setOutput(output);
        try {
            log.debug("Resetting options for action");
            action.reset();
        } catch (Throwable e) {
            throw new FatalExecutionException("Could not reset options for action: " + execution.getTarget());
        }
        log.info("Setting option values");
        final Options options = execution.getOptions();
        for (Map.Entry<String, String> entry : options.getOptions().entrySet()) {
            log.debug("Setting --" + entry.getKey() + "=" + entry.getValue());
            action.setOption(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Character, String> entry : options.getAliases().entrySet()) {
            log.debug("Setting -" + entry.getKey() + "=" + entry.getValue());
            action.setOption(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, String> entry : options.getIndexes().entrySet()) {
            log.debug("Setting %" + entry.getKey() + "=" + entry.getValue());
            action.setOption(entry.getKey(), entry.getValue());
        }
        log.info("Delegating execution to the action");
        action.execute();
    }

}
