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

import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.value.ValueReaderContextAware;

import java.io.PrintStream;
import java.util.Set;

/**
 * A disassemble strategy is an abstraction of delegating the task of extracting action information
 * from action beans.
 *
 * This way, new ways of defining and executing actions can be introduced
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 16:47)
 */
public interface DisassembleStrategy<A> extends ValueReaderContextAware {

    void reset(A action);

    Set<OptionDescriptor> getOptions(A action);

    void setOption(A action, String name, String value);

    void perform(A action, PrintStream output) throws Exception;

}
