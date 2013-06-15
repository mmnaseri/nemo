package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 7:22 PM)
 */
public class GetterMethodFilter implements Filter<Method> {

    @Override
    public boolean accepts(Method item) {
        return !Modifier.isStatic(item.getModifiers()) && Modifier.isPublic(item.getModifiers())
                && item.getParameterTypes().length == 0 && !item.getReturnType().equals(void.class)
                && item.getName().matches("get[A-Z].*");
    }

}
