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
import com.agileapes.nemo.api.Option;
import com.agileapes.nemo.option.Metadata;
import com.agileapes.nemo.option.OptionDescriptor;
import com.agileapes.nemo.util.Filter;
import com.agileapes.nemo.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/6, 17:06)
 */
public class AnnotatedSettersDisassembleStrategy extends AbstractDisassembleStrategy {

    @Override
    public Set<OptionDescriptor> getOptions(Action action) {
        final Method[] methods = ReflectionUtils.getMethods(action.getClass(), new Filter<Method>() {
            @Override
            public boolean accepts(Method item) {
                return Modifier.isPublic(item.getModifiers()) && item.isAnnotationPresent(Option.class)
                        && item.getName().matches("set[A-Z].*") && item.getParameterTypes().length == 1
                        && item.getReturnType().equals(void.class);
            }
        });
        final HashSet<OptionDescriptor> descriptors = new HashSet<OptionDescriptor>();
        for (Method method : methods) {
            final HashSet<Metadata> metadata = new HashSet<Metadata>();
            final Option option = method.getAnnotation(Option.class);
            for (Annotation annotation : method.getAnnotations()) {
                metadata.add(Metadata.fromAnnotation(annotation));
            }
            final OptionDescriptor descriptor = new OptionDescriptor(ReflectionUtils.getPropertyName(method.getName()), option.alias() == ' ' ? null : option.alias(), option.index() < 0 ? null : option.index(), option.required(), method.getParameterTypes()[0], metadata);
            descriptors.add(descriptor);
        }
        return descriptors;
    }

    @Override
    public void setOption(Action action, String name, Object value) {
        final String setterName = ReflectionUtils.getSetterName(name);
        final Method[] methods = ReflectionUtils.getMethods(action.getClass(), new Filter<Method>() {
            @Override
            public boolean accepts(Method item) {
                return Modifier.isPublic(item.getModifiers()) && item.getName().equals(setterName)
                        && item.getReturnType().equals(void.class) && item.getParameterTypes().length == 1;
            }
        });
        if (methods.length == 0) {
            throw new IllegalArgumentException("No such argument: " + name);
        }
        try {
            methods[0].invoke(action, value);
        } catch (Throwable e) {
            throw new IllegalStateException("Could not set value for option: " + name, e);
        }
    }

}
