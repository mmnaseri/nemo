package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Method;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 19:02)
 */
public class MethodNameFilter implements Filter<Method> {

    private final String name;

    public MethodNameFilter(String name) {
        this.name = name;
    }

    @Override
    public boolean accepts(Method item) {
        return item.getName().equals(name);
    }
}
