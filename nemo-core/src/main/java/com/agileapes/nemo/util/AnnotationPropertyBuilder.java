package com.agileapes.nemo.util;

import com.agileapes.nemo.contract.Callback;
import com.agileapes.nemo.contract.Filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (6/13/13, 12:24 PM)
 */
public class AnnotationPropertyBuilder {

    private final Map<String, Object> values = new HashMap<String, Object>();
    private final String name;

    public AnnotationPropertyBuilder(final Annotation annotation) {
        name = "@" + annotation.annotationType().getCanonicalName();
        ReflectionUtils.withMethods(annotation.annotationType())
                .filter(new Filter<Method>() {
                    @Override
                    public boolean accepts(Method item) {
                        return Modifier.isPublic(item.getModifiers()) && !Modifier.isStatic(item.getModifiers())
                                && item.getParameterTypes().length == 0 && !item.getReturnType().equals(void.class);
                    }
                })
                .each(new Callback<Method>() {
                    @Override
                    public void perform(Method item) {
                        try {
                            final Object value = item.invoke(annotation);
                            values.put(item.getName(), value);
                        } catch (Throwable ignored) {
                        }
                    }
                });
    }

    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public void addTo(Properties properties) {
        properties.put(name, getValues());
    }

}
