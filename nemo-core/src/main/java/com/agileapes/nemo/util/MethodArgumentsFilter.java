package com.agileapes.nemo.util;

import com.agileapes.couteau.basics.api.Filter;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/18/13, 11:29 AM)
 */
public class MethodArgumentsFilter implements Filter<Method> {

    private final Class[] types;

    public MethodArgumentsFilter(Class... types) {
        this.types = types;
    }

    @Override
    public boolean accepts(Method item) {
        return Arrays.equals(item.getParameterTypes(), types);
    }
}
