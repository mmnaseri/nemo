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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 19:10)
 */
public class DefaultValueReaderContext implements ValueReaderContext {

    private final Map<Class, ValueReader> readers = new ConcurrentHashMap<Class, ValueReader>();

    @Override
    public void add(ValueReader reader) {
        System.out.println();
        for (Class type : reader.getTypes()) {
            if (readers.containsKey(type)) {
                throw new IllegalArgumentException("Duplicate readers for type: " + type.getCanonicalName());
            }
            readers.put(type, reader);
        }
    }

    @Override
    public Class[] getTypes() {
        return readers.keySet().toArray(new Class[readers.size()]);
    }

    @Override
    public <E> E read(String text, Class<E> type) {
        if (readers.containsKey(type)) {
            return readers.get(type).read(text, type);
        }
        throw new IllegalArgumentException("No reader was found for type: " + type.getCanonicalName());
    }

}
