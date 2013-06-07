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

package com.agileapes.nemo.value.impl;

import com.agileapes.nemo.value.ValueReader;
import com.agileapes.nemo.value.ValueReaderContext;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This is the default value reader context for the application
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 19:10)
 */
public class DefaultValueReaderContext implements ValueReaderContext {

    private final Set<ValueReader> valueReaders = new CopyOnWriteArraySet<ValueReader>();

    @Override
    public void add(ValueReader reader) {
        valueReaders.add(reader);
    }

    private ValueReader getValueReader(Class<?> type) {
        for (ValueReader valueReader : valueReaders) {
            if (valueReader.handles(type)) {
                return valueReader;
            }
        }
        return null;
    }

    @Override
    public boolean handles(Class<?> type) {
        return getValueReader(type) != null;
    }

    @Override
    public <E> E read(String text, Class<E> type) {
        final ValueReader reader = getValueReader(type);
        if (reader == null) {
            throw new IllegalArgumentException("No reader was found for type: " + type.getCanonicalName());
        }
        return reader.read(text, type);
    }

}
