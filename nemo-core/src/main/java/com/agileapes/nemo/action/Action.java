package com.agileapes.nemo.action;

import com.agileapes.nemo.contract.Executable;

import java.io.PrintStream;

/**
 * The action is an abstraction of the unit of work performable by the command-line invocation of an
 * application.
 *
 * It has options and is an executable entity.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:40)
 */
public abstract class Action implements Executable {

    private boolean defaultAction;
    private boolean internal;
    private String name;
    protected PrintStream output;

    /**
     * @return {@code true} if this action is the default action. Note that in all the context of the executor
     * (as denoted by {@link com.agileapes.nemo.exec.ExecutorContext}) only one action can be marked as the default
     */
    public boolean isDefaultAction() {
        return defaultAction;
    }

    /**
     * This option will determine whether this action is the default action or not
     * @param defaultAction    should be set to {@code true} to determine that this is the default action
     */
    public void setDefaultAction(boolean defaultAction) {
        this.defaultAction = defaultAction;
    }

    /**
     * @return {@code true} means that this action is an internal action. Internal actions cannot be invoked by
     * the command line, and are meant as a way to break down the work structure into more manageable pieces in case
     * actions are too big or share sub-actions as pieces of work to be done
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * Setting this option to {@code true} will mark this action as an internal action
     * @param internal    {@code true} means this action is an internal action
     */
    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    /**
     * @return the unique name of this action
     */
    public String getName() {
        return name;
    }

    /**
     * @param name     will change this actions name. Remember that action names are frozen after they have been
     *                 registered with the context. This means that changes to action names will be disregarded once
     *                 they have been registered with their original name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return will return the output designated by the context for this action, to which it will write.
     */
    public PrintStream getOutput() {
        return output;
    }

    /**
     * This will change the output of this action. It is advisable that actions write their outputs to the
     * output provided via this method, rather than the standard output, whose redirection is much more trouble
     * and less elegant.
     * @param output    the output
     */
    public void setOutput(PrintStream output) {
        this.output = output;
    }

}
