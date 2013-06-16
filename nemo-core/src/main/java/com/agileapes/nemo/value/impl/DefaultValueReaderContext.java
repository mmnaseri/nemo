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

import com.agileapes.nemo.contract.Filter;
import com.agileapes.nemo.contract.impl.AbstractThreadSafeContext;
import com.agileapes.nemo.value.ValueReader;
import com.agileapes.nemo.value.ValueReaderContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.agileapes.nemo.util.CollectionDSL.with;

/**
 * This is the default value reader context for the application
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 19:10)
 */
public class DefaultValueReaderContext extends AbstractThreadSafeContext<ValueReader> implements ValueReaderContext {

    private static final Log log = LogFactory.getLog(ValueReaderContext.class);

    @Override
    protected Class<ValueReader> getType() {
        return ValueReader.class;
    }

    private static class ValueReaderFilter implements Filter<ValueReader> {

        private final Class targetType;

        private ValueReaderFilter(Class targetType) {
            this.targetType = targetType;
        }

        @Override
        public boolean accepts(ValueReader item) {
            return item.handles(targetType);
        }

    }

    public DefaultValueReaderContext() {
        namesAreTypeSpecific = true;
    }

    @Override
    public boolean handles(Class<?> type) {
        return !with(getMap().values())
                .filter(new ValueReaderFilter(type))
                .list().isEmpty();
    }

    @Override
    public <E> E read(String text, final Class<E> type) {
        log.info("Reading value of type " + type.getCanonicalName() + " from input: " + text);
        try {
            return with(getMap().values())
                    .filter(new ValueReaderFilter(type))
                    .first()
                    .read(text, type);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("No value reader for: " + type.getCanonicalName());
        }
    }

}
