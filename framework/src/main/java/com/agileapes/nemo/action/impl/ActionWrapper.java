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

package com.agileapes.nemo.action.impl;

import com.agileapes.nemo.action.Action;
import com.agileapes.nemo.action.DisassembleStrategy;
import com.agileapes.nemo.option.OptionDescriptor;

import java.io.PrintStream;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 17:02)
 */
public class ActionWrapper extends Action {

    public static final String TRUE = "true";
    private final Action action;
    private final DisassembleStrategy strategy;

    public ActionWrapper(Action action, DisassembleStrategy strategy) {
        this.action = action;
        this.strategy = strategy;
    }

    @Override
    public String getName() {
        return action.getName();
    }

    @Override
    public boolean isDefaultAction() {
        return action.isDefaultAction();
    }

    @Override
    public boolean isInternal() {
        return action.isInternal();
    }

    public void reset() {
        strategy.reset(action);
    }

    public Set<OptionDescriptor> getOptions() {
        return strategy.getOptions(action);
    }

    public void setOption(String name, String value) {
        strategy.setOption(action, name, value);
    }

    public void setFlag(String flag) {
        setOption(flag, TRUE);
    }

    public void setIndex(int index, String value) {
        setOption("%" + index, value);
    }

    @Override
    public void perform(PrintStream output) throws Exception {
        action.perform(output);
    }

}
