package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 19:02)
 */
public class GetterMethodFilter implements Filter<Method> {

    @Override
    public boolean accepts(Method item) {
        return Modifier.isPublic(item.getModifiers()) && !Modifier.isStatic(item.getModifiers())
                && item.getParameterTypes().length == 0 && item.getName().matches("get[A-Z].*")
                || (item.getReturnType().equals(boolean.class) && item.getName().matches("is[A-Z].*"));
    }

}
