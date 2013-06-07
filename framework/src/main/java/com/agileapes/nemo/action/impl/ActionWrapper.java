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
import java.util.HashSet;
import java.util.Set;

/**
 * The action wrapper is a handy class delegating everything to the strategy it has taken as its
 * input while still keeping up the appearance of doing everything centrally. This is just another
 * level indirection which will help hide strategies from the rest of the system.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 17:02)
 */
public class ActionWrapper<A> extends Action {

    public static final String TRUE = "true";
    private final A action;
    private final DisassembleStrategy<A> strategy;
    private Set<OptionDescriptor> options;
    private Set<String> required = new HashSet<String>();


    public ActionWrapper(A action, DisassembleStrategy<A> strategy) {
        this.action = action;
        this.strategy = strategy;
        options = strategy.getOptions(action);
        for (OptionDescriptor option : options) {
            if (option.isRequired()) {
                required.add(option.getName());
            }
        }
    }

    /**
     * @return {@code true} if this action is the default action
     */
    @Override
    public boolean isDefaultAction() {
        return strategy.isDefaultAction(action);
    }

    /**
     * @return {@code true} if this action must not be called from the command-line
     */
    @Override
    public boolean isInternal() {
        return strategy.isInternal(action);
    }

    /**
     * This method will reset the action's options to its initial state, if possible, and if not
     * to a neutral state decided by the underlying strategy.
     */
    public void reset() {
        strategy.reset(action);
    }

    /**
     * @return a set of all options associated with this action
     */
    public Set<OptionDescriptor> getOptions() {
        return options;
    }

    /**
     * Will set the value of the specified option. If the given option starts with a '-' and
     * is followed by a single character, that character is assumed to be the alias of an option.
     * @param name     the name or '-alias' of an option.
     * @param value    the textual representation of the option's value
     */
    public void setOption(String name, String value) {
        final String option;
        if (name.matches("\\-.")) {
            option = strategy.setOption(action, name.charAt(1), value);
        } else {
            option = strategy.setOption(action, name, value);
        }
        required.remove(option);
    }

    /**
     * Will set the given flag to true
     * @param flag    the name of the flag
     */
    public void setFlag(String flag) {
        final String option;
        if (flag.matches("\\-.")) {
            option = strategy.setOption(action, flag.charAt(1), TRUE);
        } else {
            option = strategy.setOption(action, flag, TRUE);
        }
        required.remove(option);
    }

    /**
     * Will set the value of the option having the given numeric index
     * @param index    the index for the option
     * @param value    the value of the option
     */
    public void setIndex(int index, String value) {
        required.remove(strategy.setOption(action, index, value));
    }

    /**
     * @param name    the name of the desired metadata
     * @return the object representing that metadata or {@code null} if it doesn't exist.
     */
    public Metadata getMetadata(String name) {
        return strategy.getMetadata(action, name);
    }

    /**
     * @param name    the name of the desired metadata
     * @return {@code true} if this metadata exists
     */
    public boolean hasMetadata(String name) {
        return strategy.hasMetadata(action, name);
    }

    /**
     * @return a set of all metadata associated with this action
     */
    public Set<Metadata> getMetadata() {
        return strategy.getMetadata(action);
    }

    /**
     * This method is invoked by the system whenever all of the required parameters
     * have been set from the outside and a route to this action is requested by the
     * platform
     * @param output    the action is supposed to write its output to this print stream
     *                  so that output redirection can be managed centrally
     * @throws Exception
     */
    @Override
    public void perform(PrintStream output) throws Exception {
        if (!required.isEmpty()) {
            throw new IllegalStateException("Value missing for required options: " + required);
        }
        strategy.perform(action, output);
    }

}
