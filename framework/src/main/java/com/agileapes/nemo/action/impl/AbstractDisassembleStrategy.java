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
import com.agileapes.nemo.value.ValueReaderContext;

import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 17:26)
 */
public abstract class AbstractDisassembleStrategy implements DisassembleStrategy {

    private ValueReaderContext readerContext;

    @Override
    public void setValueReaderContext(ValueReaderContext valueReaderContext) {
        readerContext = valueReaderContext;
    }

    @Override
    public void reset(Action action) {
        final Set<OptionDescriptor> options = getOptions(action);
        for (OptionDescriptor option : options) {
            setOption(action, option.getName(), "null");
        }
    }

    @Override
    public void setOption(Action action, String name, String value) {
        final Set<OptionDescriptor> options = getOptions(action);
        OptionDescriptor target = null;
        for (OptionDescriptor option : options) {
            if (option.getName().equals(name)) {
                target = option;
                break;
            } else if (option.getAlias() != null && name.length() == 1 && option.getAlias().toString().equals(name)) {
                target = option;
                break;
            } else if (option.getIndex() != null && name.matches("%\\d+") && option.getIndex().equals(Integer.parseInt(name.substring(1)))) {
                target = option;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("No such option: " + name);
        }
        Object converted = readerContext.read(value, target.getType());
        if (converted == null && target.getType().isPrimitive()) {
            if (target.getType().equals(int.class)) {
                converted = 0;
            } else if (target.getType().equals(long.class)) {
                converted = 0L;
            } else if (target.getType().equals(short.class)) {
                converted = (short) 0;
            } else if (target.getType().equals(float.class)) {
                converted = 0.0;
            } else if (target.getType().equals(double.class)) {
                converted = 0.0d;
            } else if (target.getType().equals(char.class)) {
                converted = (char) 0;
            } else if (target.getType().equals(boolean.class)) {
                converted = false;
            }
        }
        setOption(action, name, converted);
    }

    public abstract void setOption(Action action, String name, Object value);

}
