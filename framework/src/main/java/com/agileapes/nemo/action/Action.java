/*
 * Copyright (c) 2013. AgileApes (http://www.agileapes.scom/), and
 * associated organization.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 */

package com.agileapes.nemo.action;

import org.springframework.beans.factory.BeanNameAware;

import java.io.PrintStream;

/**
 * This is the centerpiece of the CLI framework. Each action is a performing body
 * that is expected to be addressable.
 *
 * One action can be marked as default (meaning the routing mechanism will be targeting them
 * should no action be specified by the invoking user) using {@link #setDefaultAction(boolean)}
 *
 * Actions can also receive property values through setter methods from the outside. The
 * type of the properties being set in this manner must be understandable by the framework,
 * i.e., a {@link com.agileapes.nemo.value.ValueReader} must be present capable of converting
 * the textual representation of their values into the actual type object they must be.
 *
 * To enable the framework to pass values into action containers, <strong>setter</strong>
 * methods must be marked with {@link com.agileapes.nemo.api.Option}.
 *
 * Enabling actions is possible through the bean definitions under "nemo/*.xml" context file.
 *
 * @see com.agileapes.nemo.api.Option
 * @see com.agileapes.nemo.value.ValueReader
 * @see com.agileapes.nemo.exec.Executor
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 16:35)
 */
public abstract class Action implements BeanNameAware, Comparable<Action> {

    private String name;
    private boolean defaultAction;
    private boolean internal;

    /**
     * This method returns the name of this action. Names must be unique
     * throughout the application.
     * @return the bean id for this action
     */
    public String getName() {
        return this.name;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

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

    @Override
    public int compareTo(Action o) {
        return getName().compareTo(o.getName());
    }

    /**
     * This method is invoked by the system whenever all of the required parameters
     * have been set from the outside and a route to this action is requested by the
     * platform
     * @param output    the action is supposed to write its output to this print stream
     *                  so that output redirection can be managed centrally
     * @throws Exception
     */
    public abstract void perform(PrintStream output) throws Exception;

}
