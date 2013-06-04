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
            String name = method.getName().substring(3);
            name = name.substring(0, 1).toLowerCase() + (name.length() > 1 ? name.substring(1) : "");
            setters.put(name, method);
            if (option.alias() != ' ') {
                aliases.put(String.valueOf(option.alias()), name);
            }
            if (option.required()) {
                required.add(name);
            }
        }
    }

    public void setFlag(String flag) {
        setOption(flag, TRUE);
    }

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

    public void perform() throws Exception {
        if (!required.isEmpty()) {
            throw new IllegalStateException("Required options missing value: " + required);
        }
        action.perform();
    }

}
