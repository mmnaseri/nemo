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

import com.agileapes.nemo.action.DisassembleStrategy;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.value.ValueReaderContext;

import java.util.Set;

/**
 * This is a helper class that does all the generic actions associated with disassembling
 * actions.
 *
 * @see #setOption(Object, String, Object)
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 17:26)
 */
public abstract class AbstractDisassembleStrategy<A> implements DisassembleStrategy<A> {

    public static final String NULL = "null";
    private ValueReaderContext readerContext;

    @Override
    public void setValueReaderContext(ValueReaderContext valueReaderContext) {
        readerContext = valueReaderContext;
    }

    @Override
    public void reset(A action) {
        final Set<OptionDescriptor> options = getOptions(action);
        for (OptionDescriptor option : options) {
            setOption(action, option.getName(), NULL);
        }
    }

    /**
     * This internal method will do the task of converting a textual representation of a value
     * to the actual object value expected by the underlying action
     * @param action    the action
     * @param name      the <em>name</em> of the option
     * @param type      the expected type of the value
     * @param value     the string representation of the value
     */
    private void setOption(A action, String name, Class<?> type, String value) {
        Object converted = value.equals("null") ? null : readerContext.read(value, type);
        if (converted == null && type.isPrimitive()) {
            if (type.equals(int.class)) {
                converted = 0;
            } else if (type.equals(long.class)) {
                converted = 0L;
            } else if (type.equals(short.class)) {
                converted = (short) 0;
            } else if (type.equals(float.class)) {
                converted = 0.0;
            } else if (type.equals(double.class)) {
                converted = 0.0d;
            } else if (type.equals(char.class)) {
                converted = (char) 0;
            } else if (type.equals(boolean.class)) {
                converted = false;
            }
        }
        setOption(action, name, converted);
    }

    @Override
    public void setOption(A action, String name, String value) {
        final Set<OptionDescriptor> options = getOptions(action);
        OptionDescriptor target = null;
        for (OptionDescriptor option : options) {
            if (option.getName().equals(name)) {
                target = option;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("No such option: " + name);
        }
        setOption(action, name, target.getType(), value);
    }

    @Override
    public void setOption(A action, char alias, String value) {
        final Set<OptionDescriptor> options = getOptions(action);
        OptionDescriptor target = null;
        for (OptionDescriptor option : options) {
            if (option.getAlias() != null && option.getAlias().equals(alias)) {
                target = option;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("No such option: " + alias);
        }
        setOption(action, target.getName(), target.getType(), value);
    }

    @Override
    public void setOption(A action, int index, String value) {
        final Set<OptionDescriptor> options = getOptions(action);
        OptionDescriptor target = null;
        for (OptionDescriptor option : options) {
            if (option.getIndex() != null && option.getIndex().equals(index)) {
                target = option;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("Invalid argument: " + value);
        }
        setOption(action, target.getName(), target.getType(), value);
    }

    /**
     * This method will be called by the {@link AbstractDisassembleStrategy} class whenever
     * a value has been successfully converted from its textual representation to its actual
     * value. Therefore, when implementing this class, there should be no worries of the value
     * not matching its expected type.
     * @param action    the action
     * @param name      the <em>name</em> of the option
     * @param value     the object value as extracted from the string input value
     */
    public abstract void setOption(A action, String name, Object value);

}
