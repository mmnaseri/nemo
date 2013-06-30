package com.agileapes.nemo.util;

import com.agileapes.couteau.basics.api.Filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * This filter will accept fields annotated with the given annotation
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 19:13)
 */
public class FieldAnnotationFilter implements Filter<Field> {

    private final Class<? extends Annotation> annotation;

    public FieldAnnotationFilter(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean accepts(Field item) {
        return item.isAnnotationPresent(annotation);
    }

}
