package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Field;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/18/13, 11:22 AM)
 */
public class FieldTypeFilter implements Filter<Field> {

    private final Class type;

    public FieldTypeFilter(Class type) {
        this.type = type;
    }

    @Override
    public boolean accepts(Field item) {
        return item.getType().equals(type);
    }

}
