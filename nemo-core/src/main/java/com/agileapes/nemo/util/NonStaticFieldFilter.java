package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * This filter will leave out all fields that are static.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 19:14)
 */
public class NonStaticFieldFilter implements Filter<Field> {
    @Override
    public boolean accepts(Field item) {
        return !Modifier.isStatic(item.getModifiers());
    }
}
