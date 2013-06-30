package com.agileapes.nemo.util;

import com.agileapes.couteau.basics.api.Filter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * This filter will accept any method that is a property setter.
 * This only means that it will check for the usual conventions, and does not mean that
 * it will check to see it actually is wired to a property.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 7:22 PM)
 */
public class SetterMethodFilter implements Filter<Method> {

    @Override
    public boolean accepts(Method item) {
        return !Modifier.isStatic(item.getModifiers()) && Modifier.isPublic(item.getModifiers())
                && item.getParameterTypes().length == 1 && item.getReturnType().equals(void.class)
                && item.getName().matches("set[A-Z].*");
    }

}
