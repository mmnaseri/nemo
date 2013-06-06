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

package com.agileapes.nemo.option;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 16:50)
 */
public class Metadata {

    private final String name;
    private final Map<String, Object> properties;

    public Metadata(String name, Map<String, Object> properties) {
        this.name = name;
        this.properties = properties;
    }

    public Set<String> getProperties() {
        return properties.keySet();
    }

    public void addProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + getProperties();
    }

    public static Metadata fromAnnotation(String name, Annotation annotation) {
        final Method[] methods = annotation.annotationType().getDeclaredMethods();
        final Map<String, Object> properties = new HashMap<String, Object>();
        for (Method method : methods) {
            if (method.getParameterTypes().length == 0 && !method.getReturnType().equals(void.class)) {
                try {
                    final Object value = method.invoke(annotation);
                    if (value != null) {
                        properties.put(method.getName(), value);
                    }
                } catch (Throwable ignored) {}
            }
        }
        return new Metadata(name, properties);
    }

    public static Metadata fromAnnotation(Annotation annotation) {
        return fromAnnotation("@" + annotation.annotationType().getName(), annotation);
    }

}
