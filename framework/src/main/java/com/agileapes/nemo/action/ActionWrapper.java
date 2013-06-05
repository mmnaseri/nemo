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

package com.agileapes.nemo.action;

import com.agileapes.nemo.api.Option;
import com.agileapes.nemo.util.Filter;
import com.agileapes.nemo.util.ReflectionUtils;
import com.agileapes.nemo.value.ValueReaderContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The action wrapper carries the duty of converting String values to object values using
 * {@link com.agileapes.nemo.value.ValueReader}s. This class is basically the glue which
 * holds the underlying system together.
 * 
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/4, 18:54)
 */
public class ActionWrapper {

    public static final String TRUE = "true";
    private final Action action;
    private ValueReaderContext readerContext;
    private final Map<String, Method> setters = new HashMap<String, Method>();
    private final Set<String> required = new HashSet<String>();
    private final Map<String, String> aliases = new HashMap<String, String>();
    private final Map<Integer, String> indices = new HashMap<Integer, String>();
    private int index = 0;

    /**
     * This constructor accepts the action to be wrapped and the instance of
     * {@link ValueReaderContext} currently being used by the framework.
     * @param action           the action
     * @param readerContext    the value reader context
     */
    public ActionWrapper(Action action, ValueReaderContext readerContext) {
        this.action = action;
        this.readerContext = readerContext;
        final Method[] methods = ReflectionUtils.getMethods(action.getClass(), new Filter<Method>() {
            @Override
            public boolean accepts(Method item) {
                return item.isAnnotationPresent(Option.class) && item.getName().matches("set[A-Z].*")
                        && item.getParameterTypes().length == 1 && item.getReturnType().equals(void.class);
            }
        });
        for (Method method : methods) {
            final Option option = method.getAnnotation(Option.class);
            final String name = ReflectionUtils.getPropertyName(method.getName());
            setters.put(name, method);
            if (option.alias() != ' ') {
                final String alias = String.valueOf(option.alias());
                if (aliases.containsKey(alias)) {
                    throw new IllegalStateException("Duplicate option alias: " + alias);
                }
                aliases.put(alias, name);
            }
            if (option.index() >= 0) {
                if (indices.containsKey(option.index())) {
                    throw new IllegalStateException("Duplicate option index: " + option.index());
                }
                indices.put(option.index(), name);
            }
            if (option.required()) {
                required.add(name);
            }
        }
        for (int i = 0; i < indices.size(); i ++) {
            if (!indices.containsKey(i)) {
                throw new IllegalStateException("Option indices must be sequential, and start from 0");
            }
        }
    }

    /**
     * This assumes that a flag of the given name (or alias) exists and that it should be
     * set to true.
     * @param flag    the alias or name of the flag
     */
    public void setFlag(String flag) {
        setOption(flag, TRUE);
    }

    /**
     * This method sets the value of the given option, if it exists
     * @param option    the option name or alias
     * @param value     the textual representation of the value it should take
     */
    public void setOption(String option, String value) {
        if (!setters.containsKey(option)) {
            if (aliases.containsKey(option)) {
                option = aliases.get(option);
            } else {
                throw new IllegalArgumentException("No such argument: " + option);
            }
        }
        required.remove(option);
        final Method method = setters.get(option);
        try {
            final Object actualValue = readerContext.read(value, method.getParameterTypes()[0]);
            method.invoke(action, actualValue);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to set option: " + option, e);
        }
    }

    public void setIndex(String value) {
        final String property = indices.get(index++);
        if (property == null) {
            throw new IllegalArgumentException("Invalid argument: " + value);
        }
        setOption(property, value);
    }

    /**
     * This method will delegate the performing of the action to the wrapped action instance,
     * while checking for all the required options to be set.
     * @throws Exception
     */
    public void perform() throws Exception {
        if (!required.isEmpty()) {
            throw new IllegalStateException("Required options missing value: " + required);
        }
        action.perform();
    }

    /**
     * This method returns the setter methods which will give access to the property setters
     * corresponding given options
     * @return a map from option <em>names</em> to setter methods
     */
    public Map<String, Method> getSetters() {
        return setters;
    }

}
