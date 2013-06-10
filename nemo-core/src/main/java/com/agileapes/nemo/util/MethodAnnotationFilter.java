package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 18:59)
 */
public class MethodAnnotationFilter implements Filter<Method> {

    private final Class<? extends Annotation> annotation;

    public MethodAnnotationFilter(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean accepts(Method item) {
        return item.isAnnotationPresent(annotation);
    }
}
