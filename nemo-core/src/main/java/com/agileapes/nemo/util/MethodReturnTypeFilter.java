package com.agileapes.nemo.util;

import com.agileapes.couteau.basics.api.Filter;

import java.lang.reflect.Method;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/18/13, 11:28 AM)
 */
public class MethodReturnTypeFilter implements Filter<Method> {

    private final Class type;

    public MethodReturnTypeFilter(Class type) {
        this.type = type;
    }

    @Override
    public boolean accepts(Method item) {
        return item.getReturnType().equals(type);
    }

}
