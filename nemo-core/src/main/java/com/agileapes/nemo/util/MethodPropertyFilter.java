package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Method;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 19:04)
 */
public class MethodPropertyFilter implements Filter<Method> {

    private final String propertyName;

    public MethodPropertyFilter(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public boolean accepts(Method item) {
        return (item.getName().matches("(get|set)[A-Z].*") && StringUtils.uncapitalize(item.getName().substring(3)).equals(propertyName))
                || (item.getReturnType().equals(boolean.class) && item.getName().equals("is" + StringUtils.capitalize(propertyName)));
    }
}
