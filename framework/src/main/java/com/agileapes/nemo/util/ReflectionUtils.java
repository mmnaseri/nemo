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

package com.agileapes.nemo.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 19:21)
 */
public class ReflectionUtils {

    private ReflectionUtils() {}

    public static Method[] getMethods(Class type, Filter<Method> methodFilter) {
        if (type == null || methodFilter == null) {
            throw new NullPointerException();
        }
        final List<Method> methods = new ArrayList<Method>();
        while (type != null) {
            for (Method method : type.getDeclaredMethods()) {
                if (methodFilter.accepts(method)) {
                    methods.add(method);
                }
            }
            type = type.getSuperclass();
        }
        return methods.toArray(new Method[methods.size()]);
    }

}
