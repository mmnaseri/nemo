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
import com.agileapes.nemo.option.Metadata;
import com.agileapes.nemo.option.OptionDescriptor;

import java.io.PrintStream;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 17:02)
 */
public class ActionWrapper<A> extends Action {

    public static final String TRUE = "true";
    private final A action;
    private final DisassembleStrategy<A> strategy;

    public ActionWrapper(A action, DisassembleStrategy<A> strategy) {
        this.action = action;
        this.strategy = strategy;
    }

    @Override
    public String getName() {
        return strategy.getName(action);
    }

    @Override
    public boolean isDefaultAction() {
        return strategy.isDefaultAction(action);
    }

    @Override
    public boolean isInternal() {
        return strategy.isInternal(action);
    }

    public void reset() {
        strategy.reset(action);
    }

    public Set<OptionDescriptor> getOptions() {
        return strategy.getOptions(action);
    }

    public void setOption(String name, String value) {
        if (name.matches("\\-.")) {
            strategy.setOption(action, name.charAt(1), value);
        } else {
            strategy.setOption(action, name, value);
        }
    }

    public Metadata getMetadata(String name) {
        return strategy.getMetadata(action, name);
    }

    public boolean hasMetadata(String name) {
        return strategy.hasMetadata(action, name);
    }

    public Set<Metadata> getMetadata() {
        return strategy.getMetadata(action);
    }

    public void setFlag(String flag) {
        strategy.setOption(action, flag.charAt(0), TRUE);
    }

    public void setIndex(int index, String value) {
        strategy.setOption(action, index, value);
    }

    @Override
    public void perform(PrintStream output) throws Exception {
        strategy.perform(action, output);
    }

}
