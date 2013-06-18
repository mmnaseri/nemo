package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Method;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/18/13, 11:27 AM)
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
