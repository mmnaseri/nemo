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

import com.agileapes.nemo.option.Metadata;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.util.Filter;
import com.agileapes.nemo.value.ValueReaderContextAware;

import java.io.PrintStream;
import java.util.Set;

/**
 * A disassemble strategy is an abstraction of delegating the task of extracting action information
 * from action beans.
 *
 * This way, new methods for defining and executing actions can be introduced.
 *
 * Each action must declare the strategy based on which it was declared. This is accomplished
 * via the {@link com.agileapes.nemo.api.Disassembler} annotation. However, strategies form a
 * two-way relationship with actions, in that while an action specifies which strategy it wants
 * to be disassembled with, the strategy itself must accept that action as its input. That is why
 * this interface extends {@link Filter} to filter out all actions it does not want to handle.
 *
 * This interface will take responsibility for providing information and indirect access to
 * actions, for which a clearer way might not exist for actions themselves.
 *
 * @see com.agileapes.nemo.api.Disassembler
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 16:47)
 */
public interface DisassembleStrategy<A> extends ValueReaderContextAware, Filter<Object> {

    /**
     * This method will reset all the options in the given action to their initial state.
     * The specifics are implementation-specifics.
     * @param action    the action
     */
    void reset(A action);

    /**
     * @param action    the action
     * @return all the options for the given action
     */
    Set<OptionDescriptor> getOptions(A action);

    /**
     * This method will set the value for the given option based on its actual name
     * @param action    the action
     * @param name      the name of the option
     * @param value     the string representation for this option's value
     */
    void setOption(A action, String name, String value);

    /**
     * This method will set the value for the given option based on its numerical index
     * @param action    the action
     * @param index     the index of the option
     * @param value     the string representation for this option's value
     */
    void setOption(A action, int index, String value);

    /**
     * This method will set the value for the given option based on its alias
     * @param action    the action
     * @param alias     the alias of the option
     * @param value     the string representation for this option's value
     */
    void setOption(A action, char alias, String value);

    /**
     * This method will carry out the task assigned to the action by delegation
     * @param action    the action
     * @param output    the output to which all screen output should be written by convention
     * @throws Exception
     */
    void perform(A action, PrintStream output) throws Exception;

    /**
     * @param action    the action
     * @return the unique, addressable name of the action
     */
    String getName(A action);

    /**
     * @param action    the action
     * @return {@code true} if this action is supposed to be the default action
     */
    boolean isDefaultAction(A action);

    /**
     * @param action    the action
     * @return {@code true} if this action should not be invoked from the command-line
     */
    boolean isInternal(A action);

    /**
     * This method will return metadata associated with this action based on its name
     * @param action    the action
     * @param name      the name of the metadata
     * @return matching metadata container or {@code null} if no such metadata has been assigned
     * to this action
     */
    Metadata getMetadata(A action, String name);

    /**
     * Will determine whether a given metadata is made available to the action
     * @param action    the action
     * @param name      the name of the metadata
     * @return {@code true} if it exists
     */
    boolean hasMetadata(A action, String name);

    /**
     * This method will return all metadata associated with this action
     * @param action    the action
     * @return the set of metadata
     */
    Set<Metadata> getMetadata(A action);

}
