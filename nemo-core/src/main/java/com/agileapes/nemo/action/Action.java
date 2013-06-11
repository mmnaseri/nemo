package com.agileapes.nemo.action;

import com.agileapes.nemo.contract.Executable;

import java.io.PrintStream;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 16:40)
 */
public abstract class Action implements Executable {

    private boolean defaultAction;
    private boolean internal;
    private String name;
    protected PrintStream output;

    public boolean isDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(boolean defaultAction) {
        this.defaultAction = defaultAction;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PrintStream getOutput() {
        return output;
    }

    public void setOutput(PrintStream output) {
        this.output = output;
    }

}
