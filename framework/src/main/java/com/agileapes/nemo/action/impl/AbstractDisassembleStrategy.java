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
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 17:26)
 */
public abstract class AbstractDisassembleStrategy<A> implements DisassembleStrategy<A> {

    private ValueReaderContext readerContext;

    @Override
    public void setValueReaderContext(ValueReaderContext valueReaderContext) {
        readerContext = valueReaderContext;
    }

    @Override
    public void reset(A action) {
        final Set<OptionDescriptor> options = getOptions(action);
        for (OptionDescriptor option : options) {
            setOption(action, option.getName(), "null");
        }
    }

    private void setOption(A action, String name, Class<?> type, String value) {
        Object converted = readerContext.read(value, type);
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

    public abstract void setOption(A action, String name, Object value);

}
