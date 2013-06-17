package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Method;

/**
 * This filter will accept all methods that could be a getter or a setter for the property whose name
 * has been specified.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 7:25 PM)
 */
public class MethodPropertyFilter implements Filter<Method> {

    private final String propertyName;

    public MethodPropertyFilter(String propertyName) {
        this.propertyName = propertyName.substring(0, 1).toUpperCase().concat(propertyName.substring(1));
    }

    @Override
    public boolean accepts(Method item) {
        return (item.getName().length() > 3 && item.getName().substring(3).equals(propertyName))
                || (item.getName().length() > 2 && item.getReturnType().equals(boolean.class)
                && item.getName().equals("is" + propertyName));
    }

}
