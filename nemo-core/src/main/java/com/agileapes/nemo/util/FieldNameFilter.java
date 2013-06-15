package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Field;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/15/13, 7:28 PM)
 */
public class FieldNameFilter implements Filter<Field> {

    private final String propertyName;

    public FieldNameFilter(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public boolean accepts(Field item) {
        return item.getName().equals(propertyName);
    }

}
