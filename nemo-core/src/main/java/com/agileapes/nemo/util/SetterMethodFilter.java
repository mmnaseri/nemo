package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:47)
 */
public class SetterMethodFilter implements Filter<Method> {

    @Override
    public boolean accepts(Method item) {
        return Modifier.isPublic(item.getModifiers()) && !Modifier.isStatic(item.getModifiers())
                && item.getName().matches("set[A-Z].*") && item.getReturnType().equals(void.class)
                && item.getParameterTypes().length == 1;
    }

}
