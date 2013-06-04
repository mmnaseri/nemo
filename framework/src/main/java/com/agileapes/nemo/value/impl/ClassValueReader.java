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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;

/**
 * This value reader will take as input the fully qualified name of a class within the classpath
 * as seen by the {@link #classLoader} and return the class object
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 19:08)
 */
public class ClassValueReader implements ValueReader, ApplicationContextAware {

    private ClassLoader classLoader = getClass().getClassLoader();

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setClassLoader(applicationContext.getClassLoader());
    }

    @Override
    public Class[] getTypes() {
        return new Class[]{Class.class};
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E read(String text, Class<E> type) {
        if (type.equals(Class.class)) {
            try {
                return (E) ClassUtils.forName(text, classLoader);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Specified class was not found: " + text, e);
            }
        }
        throw new IllegalArgumentException(text + " is not a valid input for " + getClass().getSimpleName());
    }
}
